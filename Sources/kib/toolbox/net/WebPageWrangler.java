package kib.toolbox.net;

import java.net.*;
import java.util.Iterator;
import java.io.*;

import org.apache.log4j.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

public class WebPageWrangler {

	private static Logger log = Logger.getLogger(WebPageWrangler.class);

	public static final String GET_METHOD = "GET";
	public static final String POST_METHOD = "POST";

	public String urlString;
	public String title;
	public Long date;
	public Long expirationDate;
	public Long modificationDate;
	public Integer responseCode;
	public String domain;

	private Document _doc;

	public WebPageWrangler() {
		super();
	}

	/*
	 * Getting origin: 		origin = this.cgiAdaptorURL().substring(0, this.cgiAdaptorURL().indexOf("/", "http://".length())+1);
	 */
	
	public static WebPageWrangler newDocument(String theURLToGet, String method, String postData, String origin) {
		log.debug("Working on "+theURLToGet);
		
		WebPageWrangler result = new WebPageWrangler();

		URL webPage = null;
		HttpURLConnection urlConnection = null;
		StringBuffer postDataContent = null;
		StringBuffer _pageContent = new StringBuffer();

		// Content:
		InputStream pageContentStream = null;
		BufferedReader pageReader = null;

		if (method == null)
			method = GET_METHOD;

		if ( (theURLToGet == null) || (theURLToGet == "") ) {
			log.error(" - theURLToGet is null.");
			return null;
		}

		try {
			webPage = new URL(theURLToGet);
			urlConnection = (HttpURLConnection)webPage.openConnection();
			urlConnection.setReadTimeout(15*1000);
			urlConnection.setDoOutput(true);
			urlConnection.setRequestMethod(method);
			if (origin != null)
				urlConnection.setRequestProperty("Access-Control-Allow-Origin", origin);

			if (method.equalsIgnoreCase(POST_METHOD)) {
				//postDataContent = new StringBuffer(URLEncoder.encode(postData, "UTF-8"));
				postDataContent = new StringBuffer(postData);
				urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				urlConnection.setRequestProperty("Content-Length", "" + postDataContent.length());
				DataOutputStream stream = new DataOutputStream(urlConnection.getOutputStream());
				stream.writeBytes(postDataContent.toString());
				stream.close();
			}

			result.responseCode = new Integer(urlConnection.getResponseCode());
			log.debug("Response: " + result.responseCode + " " + urlConnection.getResponseMessage());
			pageContentStream = urlConnection.getInputStream();

			pageReader = new BufferedReader( new InputStreamReader(pageContentStream) );
			String pageLine = pageReader.readLine();
			while (pageLine != null) {
				_pageContent.append(pageLine);
				_pageContent.append("\n");
				pageLine = pageReader.readLine();
			}

			pageContentStream.close();
			
			// Set some outside variables
			result.urlString = urlConnection.getURL().toString();
			result.date = new Long(urlConnection.getDate());
			result.expirationDate = new Long(urlConnection.getExpiration());
			result.modificationDate = new Long(urlConnection.getLastModified());

			result.domain = webPage.getProtocol() + webPage.getHost(); //aDomain;

			result._doc = Jsoup.parse(_pageContent.toString(), result.domain);
			result.title = result._doc.title();
			
			log.debug("urlString:        "+result.urlString);
			log.debug("date:             "+result.date);
			log.debug("expirationDate:   "+result.expirationDate);
			log.debug("modificationDate: "+result.modificationDate);
			log.debug("domain:           "+result.domain);
			log.debug("title:            "+result.title);

		} catch (FileNotFoundException ex) {
			log.error(" - Page not found: " + ex.getMessage());
			log.error("   URL: " + theURLToGet);
			//ex.printStackTrace();
			return null;
		} catch (MalformedURLException ex) {
			log.error(" - MalformedURLException error: " + ex.getMessage());
			log.error("   URL: " + theURLToGet);
			//ex.printStackTrace();
			return null;
		} catch (UnsupportedEncodingException ex) {
			log.error(" - Failed to URLEncode " + postData);
			log.error("   using data AS-IS");
			//ex.printStackTrace();
			return null;
		} catch (UnknownServiceException ex) {
			log.error(" - Failed creating output stream for post data.");
			//ex.printStackTrace();
			return null;
		} catch (IOException ex) {
			log.error(" - IOException: " + ex.getMessage());
			log.error("   URL: " + theURLToGet);
			//ex.printStackTrace();
			return null;
		}

		return result;
	}
	// -------------------------------------------------------------------------------------------------------------------------------------------------------------

	public String contentWithoutHTML() {
		return _doc.text();
	}

	public String contentWithoutHTML(String selector) {
		if (selector == null || selector.isEmpty())
			return null;
		Element elementText = _doc.select(selector).first(); // "div.pagecontent"
		if (elementText != null)
			return elementText.text();
		else
			return _doc.text();
	}

	public String content() {
		return _doc.toString();
	}

	public NSArray<String> links() {
		if (this.domain == null) {
			log.error("Domain is empty.");
			return null;
		}

		NSMutableArray<String> linkArray = new NSMutableArray<String>();
		String theURL = null;

		Iterator<Element> linksIterator = this._doc.select("a[href]").iterator();
		while (linksIterator.hasNext()) {
			Element element = linksIterator.next();
			theURL = element.attr("abs:href");
			log.debug("Raw URL: "+theURL);

			if (
					(theURL.isEmpty()) ||
					(!theURL.toLowerCase().startsWith(this.domain)) || 
					(theURL.toLowerCase().indexOf("cgi-bin") > 0) ||
					(theURL.toLowerCase().indexOf("#") > 0) ||
					theURL.toLowerCase().startsWith("javascript:") || 
					theURL.toLowerCase().endsWith(".sit") || 
					theURL.toLowerCase().endsWith(".mov") ||
					theURL.toLowerCase().endsWith(".exe") ||
					theURL.toLowerCase().endsWith(".pdf") ||
					theURL.toLowerCase().endsWith(".hqx")
					)
				log.debug("- " + theURL);
			else {
				if (theURL.contains("?"))
					theURL = theURL.substring(0, theURL.indexOf("?"));
				if (theURL.endsWith("/"))
					theURL = theURL.substring(0, theURL.length()-1);
				if (theURL.contains("#"))
					theURL = theURL.substring(0, theURL.indexOf("#"));
				if ( !linkArray.containsObject(theURL) ) {
					linkArray.addObject(theURL);
					log.debug("+ " + theURL);
				}
			}
		}
		log.debug("Links: "+linkArray.count());
		return linkArray;
	}

}

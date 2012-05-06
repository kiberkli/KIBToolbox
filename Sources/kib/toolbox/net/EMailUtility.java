package kib.toolbox.net;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOComponent;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

import er.javamail.ERMailDeliveryHTML;
import er.javamail.ERMailDeliveryPlainText;

public class EMailUtility extends Object {

	private static Logger log = Logger.getLogger(EMailUtility.class);

	// Created by Kieran Kelleher on 1/28/05.
	// Augmented by Klaus Berkling on 10/2011
	// - again in 2012
	// Free to use as you wish. Use at your own risk.
	//

	@SuppressWarnings("unchecked")
	public static void composeAndSendComponentMail(WOComponent emailPage, NSDictionary<String, String> emailHeaders ) {
		// Requires no null values

		composeAndSendComponentMail(emailPage,
				(String)emailHeaders.valueForKey("fromAddress"),
				(String)emailHeaders.valueForKey("fromPersonalName"),
				(String)emailHeaders.valueForKey("toAddress"),
				(String)emailHeaders.valueForKey("toPersonalName"),
				(NSArray<String>)emailHeaders.valueForKey("toAddresses"),
				(String)emailHeaders.valueForKey("toPersonalName"),
				(String)emailHeaders.valueForKey("toPersonalName") );
	}

	public static String composeAndSendComponentMail( WOComponent emailPage,
			String fromAddress,
			String fromPersonalName,
			String toAddress,
			String toPersonalName,
			NSArray<String> toAddresses,
			String replyToAddress,
			String subject ) {

		return composeAndSendComponentMail(emailPage, null,
				fromAddress,
				fromPersonalName,
				toAddress,
				toPersonalName,
				toAddresses,
				replyToAddress,
				subject );
	}
	
	public static String composeAndSendComponentMail(WOComponent emailPage, WOComponent emailPagePlain,
			String fromAddress,
			String fromPersonalName,
			String toAddress,
			String toPersonalName,
			NSArray<String> toAddresses,
			String replyToAddress,
			String subject ) {

		log.debug("Sending email with subject '" + subject + 
				"' and addressed to '"	+ (toAddress == null ? "" : toAddress.toString() )
				+ (toAddresses == null ? "" : toAddresses.toString() ) );

		// Create a new mail delivery instance
		ERMailDeliveryHTML eMail = new ERMailDeliveryHTML();

		// Set the WOComponent to be used for rendering the mail
		if (emailPage != null )
			eMail.setComponent(emailPage);
		if (emailPagePlain != null )
			eMail.setAlternativeComponent(emailPagePlain);

		try {
			eMail.newMail();

			// fromAddress with optional fromPersonalName
			if ( fromAddress != null && fromPersonalName != null ) {
				eMail.setFromAddress( fromAddress, fromPersonalName );
			} else if (fromAddress != null) {
				eMail.setFromAddress( fromAddress );
			}

			// optional toAddress and optional toPersonalName
			if ( toAddress != null && toPersonalName != null ) {
				eMail.setToAddress( toAddress, toPersonalName );
			} else if (toAddress != null) {
				eMail.setToAddress( toAddress );
			}

			// optional toAddresses (NSArray)
			if ( toAddresses != null ) eMail.setToAddresses( toAddresses );

			// reply to address
			if ( replyToAddress != null ) eMail.setReplyToAddress( replyToAddress );

			eMail.setSubject( subject );

			eMail.sendMail(true);

			return null;
			
		} catch (Exception e) {
			log.error("Exception sending email: " + e);
			e.printStackTrace();
			return e.getMessage();
		}
	}

	public static String composeAndSendPlainTextMail(String messageBody,
			String fromAddress,
			String fromPersonalName,
			String toAddress,
			String toPersonalName,
			NSArray<String> toAddresses,
			String replyToAddress,
			String subject ) {

		log.debug("Sending email with subject '" + subject + 
				"' and addressed to '"	+ (toAddress == null ? "" : toAddress.toString() )
				+ (toAddresses == null ? "" : toAddresses.toString() ) +"'");

		// Create a new mail delivery instance
		ERMailDeliveryPlainText eMail = new ERMailDeliveryPlainText();
		if (messageBody == null)
			eMail.setTextContent("");
		else
			eMail.setTextContent(messageBody);

		try {
			eMail.newMail();

			// fromAddress with optional fromPersonalName
			if ( fromAddress != null && fromPersonalName != null ) {
				eMail.setFromAddress( fromAddress, fromPersonalName );
			} else if (fromAddress != null) {
				eMail.setFromAddress( fromAddress );
			}

			// optional toAddress and optional toPersonalName
			if ( toAddress != null && toPersonalName != null ) {
				eMail.setToAddress( toAddress, toPersonalName );
			} else if (toAddress != null) {
				eMail.setToAddress( toAddress );
			}

			// optional toAddresses (NSArray)
			if ( toAddresses != null ) eMail.setToAddresses( toAddresses );

			// reply to address
			if ( replyToAddress != null ) eMail.setReplyToAddress( replyToAddress );

			eMail.setSubject( subject );

			eMail.sendMail(true);

			return null;
		} catch (Exception e) {
			log.error("Exception sending email: " + e);
			e.printStackTrace();
			return e.getMessage();
		}
	}
}

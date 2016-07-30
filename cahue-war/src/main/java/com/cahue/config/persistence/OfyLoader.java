/*
 */

package com.cahue.config.persistence;

import com.googlecode.objectify.impl.LoaderImpl;

/**
 * Extend the Loader command with our own logic
 *
 * @author Jeff Schnitzer
 */
public class OfyLoader extends LoaderImpl<OfyLoader> {
    /** */
    public OfyLoader(Ofy base) {
        super(base);
    }

//	/**
//	 * Gets the Person associated with the email, or null if there is no association.
//	 */
//	public Person personByEmail(String email) {
//		if (email == null || email.trim().length() == 0)
//			return null;
//
//		EmailLookup lookup = email(email);
//		if (lookup == null)
//			return null;
//		else
//			return lookup.getPerson();
//	}
//
//	/**
//	 * Gets the EmailLookup, or null if the normalized email is not in the system.
//	 */
//	public EmailLookup email(String email) {
//		return ofy().load().type(EmailLookup.class).id(EmailLookup.normalize(email)).now();
//	}


}
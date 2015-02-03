package org.iisigroup;

/*
 shivkumar kore
 Pune,India, 9890858987
 */

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException; 
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

/*
 This class responsible for writing "o=oblix" tree into the idif file.
 */
public class LdifExporter {
	// search scope specifications
	private static final int BASE = 0;
	private static final int ONELEVEL = 1;
	private static final int SUBTREE = 2;

	private DirContext context;
	private Hashtable<String,String> env;
	private PrintWriter writer;

	// coreid environment specific entries, we can replace it by
	// EnvironmentVO class instance
	// private String host = "";
	// private String port = "";
	// private String userID = "cn=Directory Manager";
	// private String password = "password";
	private String baseDN = "";

	public LdifExporter() {
		try {
			this.writer = new PrintWriter(new FileOutputStream(
					"sris_ldap.ldif"));
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		}
	}

	// constructor, write code for creating context to directory here

	public static void main(String[] args) {
		LdifExporter ex = new LdifExporter();// pass argument here
		ex.export("ou=team,dc=ris3");// external handle for exporting to file
	}

	public void connect() { 
		 env = new Hashtable<String,String>(11);
	        env.put(Context.INITIAL_CONTEXT_FACTORY,
	                "com.sun.jndi.ldap.LdapCtxFactory");
	        env.put(Context.PROVIDER_URL, "ldap://192.168.10.20:389");
	        env.put(Context.SECURITY_PRINCIPAL, "cn=Manager,dc=ris3");
	        env.put(Context.SECURITY_CREDENTIALS, "ldaproot");
	        env.put(Context.SECURITY_AUTHENTICATION, "simple");
		try {
			this.context = new InitialDirContext(env);
		} catch (NamingException e) {
			System.out.println("Directory server binding error");
			e.printStackTrace();
			// logging code goes here
		}
	}

	// here dn is base in the directry, which will be exported
	public boolean export(String dn) {
		connect();

		if (this.context == null)
			return false;

		String entryDN = null;
		String filter = "objectclass=*";

		try {
			SearchResult si;
			SearchControls ctls = new SearchControls();
			ctls.setSearchScope(SUBTREE);
			for (NamingEnumeration results = context.search(dn, filter, ctls); results
					.hasMore(); this.export(entryDN, si.getAttributes())) {
				si = (SearchResult) results.next();
				entryDN = getFixedDN(si.getName(), dn + "," + this.baseDN);
			}
			writer.flush();
			writer.close();

		} catch (NamingException e) {
			System.out.println("Failure during exporting!" + e);
			return false;
		}
		return true;
	}

	public void export(String entryDN, Attributes attribs) {
		try {
			writeLDIF(writer, entryDN, attribs);
			writer.println();
		} catch (IOException e) {
			// log error
			e.printStackTrace();
		}
	}

	private boolean writeLDIF(PrintWriter w, String dn, Attributes set)
			throws IOException {
		if (set == null)
			return false;
		try {
			this.writeString(w, "dn", dn);
			for (NamingEnumeration ae = set.getAll(); ae.hasMoreElements();) {
				Attribute attr = (Attribute) ae.next();
				String attrName = attr.getID();
				for (Enumeration vals = attr.getAll(); vals.hasMoreElements(); writeLDIF(
						((Writer) (w)), attrName, vals.nextElement()))
					;
			}

		} catch (NamingException _ex) {
			return false;
		}
		return true;
	}

	public static void writeLDIF(Writer w, String attrName, Object value)
			throws IOException {
		String str = value.toString();
		writeString(w, attrName, str);
	}

	public static void writeString(Writer w, String attrName, String value)
			throws IOException {
		writeFormatted(w, attrName + ": ", value.toCharArray());
	}

	public static void writeFormatted(Writer w, String firstline, char p[])
			throws IOException {
		int size = 76;
		int len = (size + 1) - firstline.length();
		w.write(firstline);

		if (p.length + firstline.length() <= size) {
			w.write(p);
			w.write(System.getProperty("line.separator"));
			return;
		}

		w.write(p, 0, len);
		w.write(System.getProperty("line.separator"));

		for (int i = len; i < p.length; i += size) {
			w.write(" ");
			if (i + size > p.length) {
				w.write(p, i, p.length - i);
			} else {
				w.write(p, i, size);
			}
			w.write(System.getProperty("line.separator"));
		}
	}

	private String fixName(String name) {
		if (name.length() > 0 && name.charAt(0) == ',') {
			int size = name.length() - 1;
			StringBuffer buf = new StringBuffer();
			for (int i = 1; i < size; i++) {
				if (name.charAt(i) == '/')
					buf.append("\\");
				buf.append(name.charAt(i));
			}

			return buf.toString();
		} else {
			return name;
		}
	}

	private String getFixedDN(String rdn, String base) {
		return getDN(fixName(rdn), base);
	}

	private String getDN(String rdn, String base) {
		if (rdn.length() == 0)
			return base;
		if (base.length() == 0)
			return rdn;
		else
			return rdn + ", " + base;
	}
}
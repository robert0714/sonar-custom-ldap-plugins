package org.iisigroup;


import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;
import org.sonar.api.config.PropertyDefinitions;
import org.sonar.plugins.ldap.LdapAutodiscovery;
import org.sonar.plugins.ldap.LdapSettingsManager;

public class EmulatorTest {
	LdapSettingsManager lsm = null;
	
	private   org.sonar.api.config.Settings setting =null;
	 
	@Test
	public void testGetDnsDomain() throws UnknownHostException {
		java.util.Properties pro = new java.util.Properties();
		final 	InputStream inStream = EmulatorTest.class.getResourceAsStream("/sonar.properties");
		try {
			 pro.load(inStream);
			 final Set<Entry<Object, Object>> entrySet = pro.entrySet();
			 
			PropertyDefinitions def =new PropertyDefinitions();
			
			
			setting = new org.sonar.api.config.Settings(def);
			 
			 for(Entry<Object, Object> unitSet :entrySet ){
				 String key = unitSet.getKey().toString();
				 String value = unitSet.getValue().toString();
					setting.getProperties().put(key,value);
					
			 }
			 
		} catch (IOException e) {
			e.printStackTrace();
		}
		LdapAutodiscovery ldapAutodiscovery = new LdapAutodiscovery();
		 
		 lsm = new LdapSettingsManager(setting, ldapAutodiscovery);
		
		
		
	}
}

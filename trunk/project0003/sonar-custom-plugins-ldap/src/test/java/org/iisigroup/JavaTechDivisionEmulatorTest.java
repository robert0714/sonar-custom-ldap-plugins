package org.iisigroup;

import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.codehaus.plexus.util.ReflectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sonar.api.PropertyType;
import org.sonar.api.config.APIUtils;
import org.sonar.api.config.Encryption;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.config.PropertyDefinitions;
import org.sonar.api.security.ExternalGroupsProvider;
import org.sonar.api.security.ExternalUsersProvider;
import org.sonar.api.security.LoginPasswordAuthenticator;
import org.sonar.api.security.UserDetails;
import org.sonar.plugins.ldap.LdapAutodiscovery;
import org.sonar.plugins.ldap.LdapRealm;
import org.sonar.plugins.ldap.LdapSettingsManager;


public class JavaTechDivisionEmulatorTest {
	LdapSettingsManager lsm = null;

	private org.sonar.api.config.Settings setting = null;

	 

	 
	
	@Before
	public void beforeTest() throws Exception {
		setting = Mockito.mock(org.sonar.api.config.Settings.class);
		Encryption encryption = APIUtils.getEncryption(setting);
		
		java.util.Properties pro = new java.util.Properties();
		final InputStream inStream = JavaTechDivisionEmulatorTest.class
				.getResourceAsStream("/sonar_javaTechDev.properties");
		try {
			pro.load(inStream);
			final Set<Entry<Object, Object>> entrySet = pro.entrySet();
			final Map<String, String> properties =new HashMap<String, String>();
			
			Map<String, PropertyDefinition> definitions = new HashMap<String, PropertyDefinition>();
			
			PropertyDefinitions definition = new PropertyDefinitions ();
			for (Entry<Object, Object> entryUnit : entrySet){
				String key = entryUnit.getKey().toString() ;
				String value =  entryUnit.getValue().toString() ;
				properties.put(key, value );
				PropertyDefinition pdef  =PropertyDefinition.create(key, PropertyType.STRING, new String[]{value});
				definitions.put(key, pdef);
			}
			ReflectionUtils.setVariableValueInObject(definition, "definitions", definitions);
			
			ReflectionUtils.setVariableValueInObject(setting, "properties", properties);
			ReflectionUtils.setVariableValueInObject(setting, "definitions", definition);
			ReflectionUtils.setVariableValueInObject(setting, "encryption", encryption);

		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(inStream!=null ){

				inStream.close();
			}
		}
	}

	@Test
	public void testSRISLDAP() throws UnknownHostException {
		 
		LdapAutodiscovery ldapAutodiscovery = new LdapAutodiscovery();

		lsm = new LdapSettingsManager(setting, ldapAutodiscovery);
		
		LdapRealm aLdapRealm =new LdapRealm(lsm);
		
		aLdapRealm.init();
		
		final	LoginPasswordAuthenticator loginPasswordAuthenticator = aLdapRealm.getLoginPasswordAuthenticator();
		
		
		boolean logon = loginPasswordAuthenticator.authenticate("robertlee","iisi@222114");
		
		System.out.println(logon);
		
		final	ExternalUsersProvider aUsersProvider = aLdapRealm.getUsersProvider();
		
		
		final	UserDetails user = aUsersProvider.doGetUserDetails("robertlee");
		
		System.out.println(ToStringBuilder.reflectionToString(user));
		
		final	ExternalGroupsProvider groupsProvider = aLdapRealm.getGroupsProvider();
		
		final	Collection<String> groups = groupsProvider.doGetGroups("robertlee");
		
		if(CollectionUtils.isNotEmpty(groups)){
			for(String group : groups){
				System.out.println(group);
			}
		}
	}
}

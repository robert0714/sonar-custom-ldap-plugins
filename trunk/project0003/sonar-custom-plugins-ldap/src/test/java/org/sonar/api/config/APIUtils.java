package org.sonar.api.config;

public class APIUtils {
	public static Encryption getEncryption(org.sonar.api.config.Settings setting) {
		Encryption encryption = new Encryption(setting);
		return encryption;
	}
}

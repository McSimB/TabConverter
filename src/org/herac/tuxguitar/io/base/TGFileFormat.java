package org.herac.tuxguitar.io.base;

public class TGFileFormat {

	private String name;
	private String mimeType;
	private String[] supportedFormats;

	public TGFileFormat(String name, String mimeType, String[] supportedFormats) {
		this.name = name;
		this.mimeType = mimeType;
		this.supportedFormats = supportedFormats;
	}

	public String getName() {
		return this.name;
	}

	public boolean isSupportedMimeType(String mimeType) {
		if (mimeType != null) {
			return (mimeType.toLowerCase().equals(this.mimeType.toLowerCase()));
		}
		return false;
	}

	public boolean isSupportedCode(String formatCode) {
		if (formatCode != null) {
			for (String supportedFormat : this.supportedFormats) {
				if (formatCode.toLowerCase().equals(supportedFormat.toLowerCase())) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean equals(Object obj) {
		if (obj instanceof TGFileFormat) {
			return (this.getName() != null && this.getName().equals(((TGFileFormat) obj).getName()));
		}
		return super.equals(obj);
	}
}

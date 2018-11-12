package com.mcsimb.tabconverter;

import org.herac.tuxguitar.app.tools.custom.converter.TGConverter;
import org.herac.tuxguitar.app.tools.custom.converter.TGConverterFormat;
import org.herac.tuxguitar.app.tools.custom.converter.TGConverterListener;
import org.herac.tuxguitar.io.base.TGFileFormat;
import org.herac.tuxguitar.io.base.TGFileFormatManager;
import org.herac.tuxguitar.io.gtp.GP1InputStream;
import org.herac.tuxguitar.io.gtp.GP2InputStream;
import org.herac.tuxguitar.io.gtp.GP3InputStream;
import org.herac.tuxguitar.io.gtp.GP4InputStream;
import org.herac.tuxguitar.io.gtp.GP5InputStream;
import org.herac.tuxguitar.io.gtp.GTPFileFormatDetector;
import org.herac.tuxguitar.io.gtp.GTPSettings;
import org.herac.tuxguitar.io.midi.MidiSongWriter;
import org.herac.tuxguitar.util.TGContext;

public class TabConverter {

	public TabConverter(String path, TGConverterListener listener) {
		TGContext context = new TGContext();

		TGFileFormatManager manager = TGFileFormatManager.getInstance(context);
		manager.addFileFormatDetector(new GTPFileFormatDetector(GP1InputStream.SUPPORTED_VERSIONS));
		manager.addFileFormatDetector(new GTPFileFormatDetector(GP2InputStream.SUPPORTED_VERSIONS));
		manager.addFileFormatDetector(new GTPFileFormatDetector(GP3InputStream.SUPPORTED_VERSIONS));
		manager.addFileFormatDetector(new GTPFileFormatDetector(GP4InputStream.SUPPORTED_VERSIONS));
		manager.addFileFormatDetector(new GTPFileFormatDetector(GP5InputStream.SUPPORTED_VERSIONS));
		GTPSettings settings = new GTPSettings();
		manager.addReader(new GP1InputStream(settings));
		manager.addReader(new GP2InputStream(settings));
		manager.addReader(new GP3InputStream(settings));
		manager.addReader(new GP4InputStream(settings));
		manager.addReader(new GP5InputStream(settings));
		manager.addWriter(new MidiSongWriter());

		TGConverter converter = new TGConverter(context, path, path);
		converter.setFormat(new TGConverterFormat(
				new TGFileFormat("Midi", "audio/midi", new String[]{"mid"}), "mid"));
		converter.setListener(listener);
		converter.process();
	}
}

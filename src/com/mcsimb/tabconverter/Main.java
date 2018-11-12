package com.mcsimb.tabconverter;

import org.herac.tuxguitar.app.tools.custom.converter.TGConverterListener;

public class Main {

	public static void main(String[] args) {
		String path;
		if (args.length != 0) {
			path = args[0];
		} else {
			path = "c:/test";
		}
		new TabConverter(path, new TGConverterListener() {
			@Override
			public void notifyStart() {
				System.out.println(">> Start");
			}

			@Override
			public void notifyFinish() {
				System.out.println(">> Finish");
			}

			@Override
			public void notifyFileProcess(String filename) {
			}

			@Override
			public void notifyFileResult(String filename, int errorCode) {
				System.out.println("... result: " + filename + ": " + errorCode);
			}
		});
	}
}
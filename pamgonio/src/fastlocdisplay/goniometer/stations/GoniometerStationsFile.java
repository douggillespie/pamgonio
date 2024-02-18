package fastlocdisplay.goniometer.stations;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import PamView.dialog.warn.WarnOnce;
import fastlocdisplay.goniometer.GoniometerException;

/**
 * Functions to handle the goniometer stations file. This is in the format something like :
 * 
 * 
#######################################################################################################
# Argos ID Properties
#
# These are the default properites for real time processing application of fastgps
# User can update to get ARGOS Decimal ID by set mapping table from ARGOS Hexadecimal ID
# This file will not be replaced with latest one when software is upgraded
# 
# Format to insert ARGOS ID pair
# argos.id.x = yyyyyyy.zzzzzz    (x is incremental index, yyyyyy is hexadecimal id, zzzzzz is decimal id)
#######################################################################################################

# ARGOS Hexadecimal ID, # decimal ID
# Hexadecimal is should be 28 bytes (7 Hexadecimal Character)
# there should be no blank in between comma

490AE79,123456 
490AE80,123457
REAE000,123458
490A,123459
 * 
 * @author dg50
 *
 */
public class GoniometerStationsFile {

	private static final String fileName = "argosid.properties";
	
//	public static void main(String[] args) {
//		
//		GoniometerStationsFile gsf = new GoniometerStationsFile();
//		
//		String tstPath = "C:\\ProgramData\\FastGPS Realtime Solution";
//		StationsFileData fileData = null;
//		try {
//			fileData = gsf.readFile(tstPath);
//		} catch (GoniometerException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		String newPath = tstPath+File.separator+fileName+".temp";
//		File newFile = new File(newPath);
//		try {
//			gsf.writeFile(newFile, fileData);
//		} catch (GoniometerException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	
	/**
	 * Make the default file identifier using a given path 
	 * and the fixed name. 
	 * @param folderPath
	 * @return file info
	 */
	public File getArgosIdFile(String folderPath) {
		File file = new File(folderPath + File.separator + fileName);
		return file;
	}
	
	/**
	 * Read the argosid file. 
	 * @param folderPath
	 * @return file data
	 * @throws GoniometerException
	 */
	public StationsFileData readFile(String folderPath) throws GoniometerException {
		File file = getArgosIdFile(folderPath);
		if (file.exists() == false) {
			throw new GoniometerException("Unable to find argosid file: " + file.getAbsolutePath());
		}
		
		StationsFileData fileData = new StationsFileData(file.getAbsolutePath());
		
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			throw new GoniometerException("Unable to find argosid file: " + file.getAbsolutePath());
		}
		while (true) {
			String aLine = null;
			try {
				aLine = reader.readLine();
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
			if (aLine == null) {
				break;
			}
			
			StationId stationId = isStationId(aLine);
			if (stationId == null) {
				fileData.addOtherLine(aLine);
			}
			else {
				fileData.addStationId(stationId);
			}
			
		}
		try {
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return fileData;
	}

	/**
	 * Try to get a valid station id from the line. 
	 * This is generally < about 15 character (7 for hex and 6 for decimal with a comma between)
	 * but allow for  a bit more. 
	 * 
	 * @param aLine
	 * @return
	 * @throws GoniometerException 
	 */
	private StationId isStationId(String aLine) throws GoniometerException {
		if (aLine.length() < 10) {
			return null;
		}
		if (aLine.startsWith("#")) {
			return null;
		}
		// looking good if we get here. 
		String[] bits = aLine.split(",");
		if (bits.length != 2) {
			throw new GoniometerException("Unable to interpret stations line " + aLine);
		}
		for (int i = 0; i < 2; i++) {
			bits[i] = bits[i].trim();
		}
		int hexId=0, intId=0;
		try {
			hexId = Integer.decode("0x"+bits[0]);
		}
		catch (NumberFormatException e) {
			throw new GoniometerException("Unable to interpret hex id " + bits[0]);
		}
		try {
			intId = Integer.valueOf(bits[1]);
		}
		catch (NumberFormatException e) {
			throw new GoniometerException("Unable to interpret integer id " + bits[1]);
		}
		return new StationId(intId, hexId);
	}
	
	/**
	 * Write a new file into the given path from the available data. 
	 * @param filePath
	 * @param fileData
	 * @throws GoniometerException 
	 */
	public void writeFile(File filePath, StationsFileData fileData) throws GoniometerException {
		BufferedWriter writer = null;
		
		try {
			writer = new BufferedWriter(new FileWriter(filePath));
		} catch (IOException e) {
			throw new GoniometerException("Error opening argosid file for writing: " + e.getMessage());
		}
		ArrayList<String> otherLines = fileData.getOtherLines();
		for (String aLine : otherLines) {
			try {
				writer.write(aLine);
				writer.newLine();
			} catch (IOException e) {
				throw new GoniometerException("Error writing argosid file: " + e.getMessage());
			}			
		}
		ArrayList<StationId> stations = fileData.stationIds;
		for (StationId id : stations) {
			String aLine = String.format("%07X,%06d", id.getHexId(), id.getIntegerId());
			try {
				writer.write(aLine);
				writer.newLine();
			} catch (IOException e) {
				throw new GoniometerException("Error writing argosid file: " + e.getMessage());
			}			
		}
		try {
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Rewrite the file
	 * @param path folder the files live in
	 * @param fileData updated file data. 
	 */
	public boolean reWriteFile(String folderPath, StationsFileData fileData) {
		/*
		 * first write to a temp file, then rename it. 
		 */
		File truePath = getArgosIdFile(folderPath);
		String tempName = truePath.getAbsolutePath() + ".temp";
		File tempFile = new File(tempName);
		if (tempFile.exists()) {
			tempFile.delete();
		}
		
		boolean deleted = false;
		try {
			writeFile(tempFile, fileData);
		} catch (GoniometerException e) {
			e.printStackTrace();
			return false;
		}
		try {
			deleted = truePath.delete();
		}
		catch (SecurityException e) {
			WarnOnce.showWarning("Argos Stations", "Unable to delete old file " + truePath.getAbsolutePath(), WarnOnce.WARNING_MESSAGE);
		}
		if (deleted == false) {
			WarnOnce.showWarning("Argos Stations", "Unable to delete old file " + truePath.getAbsolutePath(), WarnOnce.WARNING_MESSAGE);
			return false;
		}
		// now try to rename that file to the true path
		boolean renamed = false;
		try {
			renamed = tempFile.renameTo(truePath);
		}
		catch (SecurityException e) {
			WarnOnce.showWarning("Argos Stations", "Unable to replace old stations file " + truePath.getAbsolutePath(), WarnOnce.WARNING_MESSAGE);
		}
		return renamed;
	}
}

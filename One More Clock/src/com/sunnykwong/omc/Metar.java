package com.sunnykwong.omc;

import java.io.IOException;
import java.util.ArrayList;

import android.text.format.Time;
import android.util.Log;

public class Metar {
	public String ob; // Raw Observation
	public String icao;
	public Time timestamp;
	public String condition;
	public String windDirString;
	public int OMCConditionCode; 
	public double tempC, tempF, dewC, dewF;
	public double relHumidity;
	public double phPa, pmmHg, pinHg;
	public double windDir, windSpdKT, windSpdMPS, windSpdMPH;
	public double windVarFrom, windVarTo;
	public double gustsKT, gustsMPS, gustsMPH;
	public double visMI, visKM;
    public static final ArrayList<String> CLOUDCODES = new ArrayList<String>();
    public static final ArrayList<String> WEATHERCODES = new ArrayList<String>();
    public static final ArrayList<String> WEATHERMODIFIERS = new ArrayList<String>();
	
    static {
    	CLOUDCODES.add("SKC");
    	CLOUDCODES.add("CLR");
    	CLOUDCODES.add("NSC"); 
    	CLOUDCODES.add("FEW"); //Few
    	CLOUDCODES.add("SCT"); //Scattered
    	CLOUDCODES.add("BKN"); //Broken
    	CLOUDCODES.add("OVC"); //Overcast

    	WEATHERMODIFIERS.add("BC"); //Patches
    	WEATHERMODIFIERS.add("BL"); //Blowing
    	WEATHERMODIFIERS.add("DR"); //Low Drifting
    	WEATHERMODIFIERS.add("FZ"); //Freezing
    	WEATHERMODIFIERS.add("MI"); //Shallow
    	WEATHERMODIFIERS.add("RE"); //Recent
    	WEATHERMODIFIERS.add("SH"); //Showers
    	WEATHERMODIFIERS.add("TS"); //T-storms
    	WEATHERMODIFIERS.add("VC"); //In Vicinity
    	
    	WEATHERCODES.add("BR"); //Mist
    	WEATHERCODES.add("DS"); //Duststorm
    	WEATHERCODES.add("DU"); //Dust
    	WEATHERCODES.add("DZ"); //Drizzle
    	WEATHERCODES.add("FC"); //Funnel Cloud/Tornado/Waterspout
    	WEATHERCODES.add("FG"); //Fog
    	WEATHERCODES.add("FU"); //Smoke
    	WEATHERCODES.add("GR"); //Hail
    	WEATHERCODES.add("GS"); //Small Hail
    	WEATHERCODES.add("HZ"); //Haze
    	WEATHERCODES.add("IC"); //Diamond Dust
    	WEATHERCODES.add("PE"); //Ice Pellets
    	WEATHERCODES.add("PL"); //Ice Pellets
    	WEATHERCODES.add("PO"); //Dust Devils
    	WEATHERCODES.add("RA"); //Rain
    	WEATHERCODES.add("SA"); //Sand
    	WEATHERCODES.add("SG"); //Snow Grains
    	WEATHERCODES.add("SN"); //Snow
    	WEATHERCODES.add("SQ"); //Squall
    	WEATHERCODES.add("SS"); //Sandstorm
    	WEATHERCODES.add("VA"); //Volcanic Ash
    	WEATHERCODES.add("UP"); //Unidentified precipitation
    }
    
	public static Metar parse(final String sICAO, final String sObservation) {
		if (sObservation==null) throw new MetarParsingException();
		Metar metar = new Metar();
		metar.icao = (sICAO==null?null:sICAO);
		metar.ob = sObservation;
		
		final String[] tokens = sObservation.split(" ");
		int iMarker = 0;

		// Get rid of initial stuff first.
		for (iMarker=0;iMarker<tokens.length;iMarker++) {
			final String token = tokens[iMarker];
            if (OMC.DEBUG) Log.i(OMC.OMCSHORT+"METAR","Got header phrase " + token);
			// If we get a TAF by mistake; choke
			if (token.equals("TAF")) throw new MetarParsingException();
			// Throw away the fixed "METAR" designator
			if (token.equals("METAR")) continue;
			// Throw away the fixed "SPECI" designator
			if (token.equals("SPECI")) continue;
			// Throw away any shortcut times
			if (token.length()!=4) continue;
			// If we didn't know the ICAO before, we know now.
			if (metar.icao==null) metar.icao=token;
			break;
		}
		
		// Getting into the meat of things.
		// Parse Timestamp and Wind
		
		String sModifier=null;
		for (int i=iMarker;i<tokens.length;i++) {
			final String token = tokens[i];
            if (OMC.DEBUG) Log.i(OMC.OMCSHORT+"METAR","Got phrase " + token);
            
            // Don't care whether it's automatic, or correction.
            if (token.equals("AUTO") || token.equals("COR")) continue;
            
            // Don't deal with remarks right now - if we see it, we're done parsing.
            if (token.equals("RMK")) break;
            
            // Don't deal with trend groups right now - if we see it, we're done parsing.
            if (token.equals("BECMG") || token.equals("TEMPO") || token.equals("NOSIG")) break;
//          // Check for blocks
//          if (token.equals("TEMPO") || token.equals("BECMG")) {
//          	sModifier=token;
//              if (OMC.DEBUG) Log.i(OMC.OMCSHORT+"METAR","Starting " + token + " group");
//          	continue;
//          }

            
            // Check for Timestamp
            if (token.endsWith("Z")) {
            	Time t = new Time(Time.TIMEZONE_UTC);
            	t.setToNow();
            	
            	// If the timestamp is only 5 digits long, we're missing the month. assume current day.
            	if (token.length()==5) {
                	int iHour = Integer.parseInt(token.substring(0,2)); // First two numbers are hour
                	int iMinute = Integer.parseInt(token.substring(2,4)); // Next two numbers are minute
                	if (t.hour < iHour) {
                		// We can safely assume that the observation time is in the past.
                		// So if report hour is larger than current hour, we're right at the day change boundary.
                		// If it's not, well, GIGO for now
                		t.monthDay--;
                	}
            		t.hour=iHour;
            		t.minute=iMinute;
            		t.normalize(false);

            	// If the timestamp is 7 digits long, we have day, hour, minute.
            	} else if (token.length()==7) {
                	//int iDayOfMonth = Integer.parseInt(token.substring(0,2)); // First two numbers are date of month
                	int iHour = Integer.parseInt(token.substring(2,4)); // Next two numbers are hour
                	int iMinute = Integer.parseInt(token.substring(4,6)); // Next two numbers are minute
                	// Discard monthday info for now - we don't really need it since the phone should keep reasonably good time
                	if (t.hour < iHour) {
                		// We can safely assume that the observation time is in the past.
                		// So if report hour is larger than current hour, we're right at the day change boundary.
                		// If it's not, well, GIGO for now
                		t.monthDay--;
                	}
            		t.hour=iHour;
            		t.minute=iMinute;
            		t.normalize(false);
            		
               	// If the timestamp is another length, something's wrong.
            	} else {
            		throw new MetarParsingException();
            	}
            }
        
            // Check for Wind
            
            if (token.endsWith("KT")) {
            	final String dir = token.substring(0,3);
            	final String spd = token.substring(3,6);
            	metar.windDir = Double.parseDouble(dir);
            	metar.windDirString=getWindDir(dir);
            	if (spd.startsWith("P")) metar.windSpdKT = Double.parseDouble("1"+spd.substring(1,3));
            	else metar.windSpdKT = Double.parseDouble(spd.substring(0,2));
            	metar.windSpdMPH = kt2mph(metar.windSpdKT);
            	metar.windSpdMPS = kt2mps(metar.windSpdKT);
            	metar.windVarFrom=0;
            	metar.windVarTo=0;
            	int gustpos=token.indexOf('G');
            	if (gustpos!=-1) {
            		String sGusts = token.substring(gustpos,gustpos+3);
                	if (sGusts.startsWith("P")) metar.gustsKT = Double.parseDouble("1"+sGusts.substring(1,3));
                	else metar.gustsKT = Double.parseDouble(sGusts.substring(0,2));
            		metar.gustsMPH=kt2mph(metar.gustsKT);
            		metar.gustsMPS=kt2mps(metar.gustsKT);
            	} else {
            		metar.gustsKT=0;
            		metar.gustsMPH=0;
            		metar.gustsMPS=0;
            	}
            } 
            
            if (token.endsWith("KMH")||token.endsWith("KPH")) {
            	final String dir = token.substring(0,3);
            	final String spd = token.substring(3,6);
            	metar.windDir = Double.parseDouble(dir);
            	metar.windDirString=getWindDir(dir);
            	double kph;
            	if (spd.startsWith("P")) kph = Double.parseDouble("1"+spd.substring(1,3));
            	else kph = Double.parseDouble(spd.substring(0,2));
            	metar.windSpdKT = kph2kt(kph);
            	metar.windSpdMPH = kph2mph(kph);
            	metar.windSpdMPS = kph2mps(kph);
            	metar.windVarFrom=0;
            	metar.windVarTo=0;
            	int gustpos=token.indexOf('G');
            	if (gustpos!=-1) {
            		String sGusts = token.substring(gustpos,gustpos+3);
                	if (sGusts.startsWith("P")) kph = Double.parseDouble("1"+sGusts.substring(1,3));
                	else kph = Double.parseDouble(sGusts.substring(0,2));
                	metar.gustsKT = kph2kt(kph);
            		metar.gustsMPH=kph2mph(kph);
            		metar.gustsMPS=kph2mps(kph);
            	} else {
            		metar.gustsKT=0;
            		metar.gustsMPH=0;
            		metar.gustsMPS=0;
            	}
            }
            if (token.endsWith("MPS")) {
            	final String dir = token.substring(0,3);
            	final String spd = token.substring(3,6);
            	metar.windDir = Double.parseDouble(dir);
            	metar.windDirString=getWindDir(dir);
            	if (spd.startsWith("P")) metar.windSpdMPS = Double.parseDouble("1"+spd.substring(1,3));
            	else metar.windSpdMPS = Double.parseDouble(spd.substring(0,2));
            	metar.windSpdMPH = mps2mph(metar.windSpdMPS);
            	metar.windSpdKT = mps2kt(metar.windSpdMPS);
            	metar.windVarFrom=0;
            	metar.windVarTo=0;
            	int gustpos=token.indexOf('G');
            	if (gustpos!=-1) {
            		String sGusts = token.substring(gustpos,gustpos+3);
                	if (sGusts.startsWith("P")) metar.gustsMPS = Double.parseDouble("1"+sGusts.substring(1,3));
                	else metar.gustsMPS = Double.parseDouble(sGusts.substring(0,2));
            		metar.gustsMPH=mps2mph(metar.gustsMPS);
            		metar.gustsKT=mps2kt(metar.gustsMPS);
            	} else {
            		metar.gustsKT=0;
            		metar.gustsMPH=0;
            		metar.gustsMPS=0;
            	}
            }
            	
            // Check for variable wind directions
            if (token.length()==7 && token.charAt(3)=='V' && 
            		(tokens[i-1].endsWith("KT")||token.endsWith("KMH")||token.endsWith("MPS"))) {
            	metar.windVarFrom=Integer.parseInt(token.substring(0,2)); 
            	metar.windVarTo=Integer.parseInt(token.substring(2,4)); 
            }

            // Update the token marker.
            iMarker=i;

		}
            
		// Next, Parse visibility
		
		sModifier=null;
		for (int i=iMarker;i<tokens.length;i++) {
			final String token = tokens[i];
            if (OMC.DEBUG) Log.i(OMC.OMCSHORT+"METAR","Got phrase " + token);
            
            // Don't deal with remarks right now - if we see it, we're done parsing.
            if (token.equals("RMK")) break;
            
            // Don't deal with trend groups right now - if we see it, we're done parsing.
            if (token.equals("BECMG") || token.equals("TEMPO") || token.equals("NOSIG")) break;
            
            // Look for a block of 4 digits (raw metric visibility) - we'll discard right now
            if (token.length()==4 && isNumeric(token)) continue;
            
            // Look for a block that starts with a digit and ends with a letter (directional visibility or
            // US visibility in SM) - we'll discard
            if (isNumeric(token.substring(0,1)) && 
            		isAlpha(token.substring(token.length()-1,token.length()))) continue;
            
            // Look for a block that has a slash and starts with R (runway visibility) -
            // we'll also discard right now
            if (token.startsWith("R") && token.contains("/")) continue;
            
            // Update the token marker.
            iMarker=i;
		}
		
		// Next, Parse weather
		
		sModifier=null;
		ArrayList<String> weatherTokens = new ArrayList<String>();

		for (int i=iMarker;i<tokens.length;i++) {
			String token = new String(tokens[i]);
            if (OMC.DEBUG) Log.i(OMC.OMCSHORT+"METAR","Got phrase " + token);

            //Intensity
            if (token.startsWith("+")) {
            	sModifier = "heavy";
            	token = token.substring(1);
            }
            else if (token.startsWith("-")) {
            	sModifier = "light";
            	token = token.substring(1);
            }

            if (token.length()%2!=0) throw new MetarParsingException();
            
            for (int j=0; j< token.length(); j+=2) {
            	String subtoken = token.substring(j,j+2);
            	weatherTokens.add(subtoken);
            }
            
            // OK, now we have a list of weathertokens denoting weather conditions.  
		}
            
		iMarker++;
		
		for (int i=iMarker;i<tokens.length;i++) {
			String token = new String(tokens[i]);
            if (OMC.DEBUG) Log.i(OMC.OMCSHORT+"METAR","Got phrase " + token);

            // OK, now we have added the worst cloud cover to the weather conditions as well.
            
            // Find prevailing weather!
            	// 1. Hurricanes.
            if (metar.windSpdMPH>=74) {
            	metar.condition="hurricane";
            	metar.OMCConditionCode=39;
            	// 2. Tornadoes.
            } else if (weatherTokens.contains("FC")) {
            	metar.condition="tornado";
            	metar.OMCConditionCode=39;
            	// 3. Volcanic Ash.
            } else if (weatherTokens.contains("VA")) {
            	metar.condition="volcanic ash";
            	metar.OMCConditionCode=9;
            	// 4. Ice.
            } else if (weatherTokens.contains("IC") || weatherTokens.contains("PL") || weatherTokens.contains("PE")) {
            	metar.condition="ice pellets";
            	metar.OMCConditionCode=36;
            	// 5. T-storms.
            } else if (weatherTokens.contains("TS")) {
            	metar.condition="thunderstorms";
            	metar.OMCConditionCode=21;
            	// 5a. Storms.
            } else if (weatherTokens.contains("TS") && sModifier.equals("light")) {
            	metar.condition="storm";
            	metar.OMCConditionCode=25;
            	// 6. Snow.
            } else if (weatherTokens.contains("SN") || weatherTokens.contains("SG")) {
            	if (weatherTokens.contains("SH") && sModifier.equals("light")) {
                	metar.condition="light snow showers";
                	metar.OMCConditionCode=32;
            	} else if (weatherTokens.contains("SH")) {
                	metar.condition="snow showers";
                	metar.OMCConditionCode=31;
            	} else if (sModifier.equals("light")) {
                	metar.condition="light snow";
                	metar.OMCConditionCode=29;
            	} {
                	metar.condition="snow";
                	metar.OMCConditionCode=33;
            	}
            	// 7. Dust- or Sandstorms.
            } else if (weatherTokens.contains("DS") || weatherTokens.contains("DU")) {
            	metar.condition="dust";
            	metar.OMCConditionCode=6;
            } else if (weatherTokens.contains("SS")) {
            	metar.condition="sandstorm";
            	metar.OMCConditionCode=6;
            	// 8. Fog.
            } else if (weatherTokens.contains("FG") && !weatherTokens.contains("BC")) {
            	metar.condition="fog";
            	metar.OMCConditionCode=13;
            	// 8a. Mist.
            } else if (weatherTokens.contains("BR")) {
            	metar.condition="mist";
            	metar.OMCConditionCode=8;
            	// 9. Smoke.
            } else if (weatherTokens.contains("FU")) {
            	metar.condition="smoke";
            	metar.OMCConditionCode=9;
            	// 10. Patchy Fog.
            } else if (weatherTokens.contains("FG") && weatherTokens.contains("BC")) {
            	metar.condition="patchy fog";
            	metar.OMCConditionCode=13;
            	// 11. Windy.
            } else if (weatherTokens.contains("FU")) {
            	metar.condition="smoke";
            	metar.OMCConditionCode=9;
            	// 12. Breezy.
            } else if (weatherTokens.contains("FU")) {
            	metar.condition="smoke";
            	metar.OMCConditionCode=9;
            	// 13. Drizzle.
            } else if (weatherTokens.contains("DZ")) {
            	if (weatherTokens.contains("FZ")) {
                	metar.condition="freezing drizzle";
                	metar.OMCConditionCode=37;
            	} else {
                	metar.condition="drizzle";
                	metar.OMCConditionCode=16;
            	}
            	// 14. Rain.
            } else if (weatherTokens.contains("RA")) {
            	if (weatherTokens.contains("FZ")) {
                	metar.condition="freezing rain";
                	metar.OMCConditionCode=37;
            	} else if (sModifier.equals("heavy")) {
                	metar.condition="heavy rain";
                	metar.OMCConditionCode=26;
            	} else if (sModifier.equals("light")) {
                	metar.condition="light rain";
                	metar.OMCConditionCode=14;
            	} else {
                	metar.condition="rain";
                	metar.OMCConditionCode=9;
            	}
            	// 15. Showers.
            } else if (weatherTokens.contains("SH")) {
            	if (weatherTokens.contains("SN")) {
                	metar.condition="snow showers";
                	metar.OMCConditionCode=31;
            	} else if (sModifier.equals("RA")) {
                	metar.condition="rain showers";
                	metar.OMCConditionCode=15;
            	} else {
                	metar.condition="showers";
                	metar.OMCConditionCode=17;
            	}
            	// 16. Overcast.
            } else if (weatherTokens.contains("FU")) {
            	metar.condition="smoke";
            	metar.OMCConditionCode=9;
            	// 18. Mostly Cloudy.
            } else if (weatherTokens.contains("FU")) {
            	metar.condition="smoke";
            	metar.OMCConditionCode=9;
            	// 19. Partly Cloudy.
            } else if (weatherTokens.contains("FU")) {
            	metar.condition="smoke";
            	metar.OMCConditionCode=9;
            	// 20. Smog.
            } else if (weatherTokens.contains("FU")) {
            	metar.condition="smoke";
            	metar.OMCConditionCode=9;
            	// 21. Mostly Clear.
            } else if (weatherTokens.contains("FU")) {
            	metar.condition="smoke";
            	metar.OMCConditionCode=9;
            	// 22. Clear.
            } else if (weatherTokens.contains("FU")) {
            	metar.condition="smoke";
            	metar.OMCConditionCode=9;
            }
            	
            	
            	
            // Check for Temp/Dew Point
            if (token.contains("/")) {
                // Not dealing with runway visual range blocks right now
            	if (token.startsWith("R")) continue;
            	int iDivider = token.indexOf('/');
            	String sTemp = token.substring(0,iDivider);
            	String sDew = token.substring(iDivider+1,token.length());
            	if (sTemp.startsWith("M")) metar.tempC = -1d*Double.parseDouble(sTemp.substring(1,3));
            	else metar.tempC = -1d*Double.parseDouble(sTemp.substring(0,2));
            	if (sDew.startsWith("M")) metar.dewC = -1d*Double.parseDouble(sDew.substring(1,3));
            	else metar.dewC = -1d*Double.parseDouble(sDew.substring(0,2));
            	metar.tempF = c2f(metar.tempC);
            	metar.dewF = c2f(metar.dewC);
            }

            // Check for Cloud Coverage
        
            
            // Check for NOSIG/CAVOK
            
            // Check for Extreme Conditions
            
            
            
		}
		return metar;
	}
	
	public static String getWindDir(String degrees) {
		if (degrees.equals("VRB")) return "VRB";
		int winddeg = (int)(Double.parseDouble(degrees));
		if (winddeg>337.5) return "N";
		else if (winddeg>292.5) return "NW";
		else if (winddeg>247.5) return "W";
		else if (winddeg>202.5) return "SW";
		else if (winddeg>157.5) return "S";
		else if (winddeg>112.5) return "SE";
		else if (winddeg>67.5) return "E";
		else if (winddeg>22.5) return "NE";
		else return "N";
	}
	
	public static double kt2mph(final double knots){
		return knots * 1.15077945d;
	}

	public static double kt2mps(final double knots){
		return knots * 0.514444444d;
	}

	public static double mph2kt(final double milesperhour){
		return milesperhour * 0.868976d;
	}

	public static double mph2mps(final double milesperhour){
		return milesperhour * 0.44704d;
	}

	public static double mps2kt(final double metrespersecond){
		return metrespersecond * 1.94384d;
	}

	public static double mps2mph(final double metrespersecond){
		return metrespersecond * 2.23694d;
	}

	public static double kph2kt(final double kilometresperhour){
		return kilometresperhour * 0.539957d;
	}

	public static double kph2mps(final double kilometresperhour){
		return kilometresperhour * 3.6d;
	}

	public static double kph2mph(final double kilometresperhour){
		return kilometresperhour * 0.621371d;
	}
	
	public static double c2f (final double celsius) {
		return celsius /5d * 9d + 32d;
	}
	
	public static boolean isNumeric(String inputData) {
		return inputData.matches("[-+]?\\d+(\\.\\d+)?");
	}

	public static boolean isAlpha(String inputData) {
		return inputData.matches("^[a-zA-Z]+$");
	}
}

class MetarParsingException extends RuntimeException {
	static final long serialVersionUID = 23456l;
	public MetarParsingException() {
		super();
	}
}
package helloworldusimrel5;

// Imported packages

import sim.toolkit.*;
import sim.access.*;
import javacard.framework.*;

public class HelloWorldUsimRel5 extends Applet implements ToolkitInterface,
		ToolkitConstants {
	protected static HelloWorldUsimRel5 instance;

	private ToolkitRegistry reg;

	/** Display Text Command Qualifier **/
	private final static byte DT_CQ = (byte) 0x81;

	/** DCS 8 bit DATA */
	private final static byte DCS_8_BIT_DATA = (byte) 0x04;

	private static final byte VALUE_ZERO = (byte) 0x00;

	private final static byte OFFSET_ZERO = (byte) 0x00;

	private final static byte[] msg = { (byte) 'T', (byte) 'h', (byte) 'i',
			(byte) 's', (byte) ' ', (byte) 'i', (byte) 's', (byte) ' ',
			(byte) 'f', (byte) 'i', (byte) 'r', (byte) 's', (byte) 't',
			(byte) ' ', (byte) 'H', (byte) 'e', (byte) 'l', (byte) 'l',
			(byte) 'o', (byte) ' ', (byte) 'f', (byte) 'o', (byte) 'r',
			(byte) 'm', (byte) ' ', (byte) 'G', (byte) 'l', (byte) 'o',
			(byte) 'b', (byte) 'e', (byte) 'T', (byte) 'o', (byte) 'u',
			(byte) 'c', (byte) 'h', (byte) '!', (byte) '!', (byte) '!' };

	/**
	 * Constructor of the applet
	 */

	public HelloWorldUsimRel5() {
		// initialize object
		// Get the reference of the applet ToolkitRegistry object
		this.reg = ToolkitRegistry.getEntry();
		// register events
		this.reg.setEvent(EVENT_PROFILE_DOWNLOAD);
		this.reg.setEvent(EVENT_FIRST_COMMAND_AFTER_SELECT);
	}

	/**
	 * Method called by the JCRE at the installation of the applet
	 * 
	 * @param bArray
	 *            the byte array containing the AID bytes
	 * @param bOffset
	 *            the start of AID bytes in bArray
	 * @param bLength
	 *            the length of the AID bytes in bArray
	 */

	public static void install(byte[] bArray, short bOffset, byte bLength) {
		// Create the Java SIM toolkit applet
		HelloWorldUsimRel5 StkCommandsExampleApplet = new HelloWorldUsimRel5();
		// Register this applet
		StkCommandsExampleApplet.register(bArray, (short) (bOffset + 1),
				(byte) bArray[bOffset]);
	}

	/**
	 * Method called by the SIM Toolkit Framework
	 * 
	 * @param event
	 *            the byte representation of the event triggered
	 */
	public void processToolkit(byte event) {
		switch (event) {
		case EVENT_PROFILE_DOWNLOAD:
			processDT();
			break;
		}
	}

	/**
	 * Method called by the JCRE, once selected
	 * 
	 * @param apdu
	 *            the incoming APDU object
	 */
	public void process(APDU apdu) {
		// ignore the applet select command dispached to the process
		if (selectingApplet()) {
			return;
		}
		byte buffer[] = apdu.getBuffer();
		// Proprietary Command
		// CLA INS P1 P2 P3
		// 00 44 00 00 32
		if (buffer[ISO7816.OFFSET_CLA] != VALUE_ZERO) {
			ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
		}
		if (buffer[ISO7816.OFFSET_INS] != (byte) 0x44) {
			ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
		}

		// Lc tells us the incoming apdu command length
		short tempLength_g = buffer[ISO7816.OFFSET_LC];
		if (tempLength_g <= VALUE_ZERO) { // XXX:Check use case
			ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
		} // End of if ( tempLength_g <= VALUE_ZERO )
		short readCount = apdu.setIncomingAndReceive();
		while (tempLength_g > VALUE_ZERO) {
			// process bytes in buffer[5] to buffer[readCount+4];
			tempLength_g -= readCount;
			readCount = apdu.receiveBytes(ISO7816.OFFSET_CDATA);
		} // End of while( tempLength_g > VALUE_ZERO )
		short currentSW_g = (short) 0x0000;
		byte buffOut[] = null;
		// processDT();
		// /////////////////////
		// construct the reply APDU
		// Check le
		tempLength_g = apdu.setOutgoing();
		if (tempLength_g < (short) 0x02) {
			ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
		} // End of if ( tempLength_g < VALUE_TWO )
		// Check return data
		if (buffOut == null) { // No Buffer is returned
			// build response data in buffApdu
			// Only status words will be returned
			currentSW_g = (short) ((currentSW_g == ISO7816.SW_NO_ERROR) ? 0x6404
					: 0x6405);

			apdu.setOutgoingLength(Util.setShort(buffer, OFFSET_ZERO,
					currentSW_g));
			apdu.sendBytes(OFFSET_ZERO, (short) 0x02);
		} // End of if ( buffOut == null )
	} // End of public void process( APDU apdu ) throws ISOException

	/**
	 * This function process the display text proactive command
	 */
	private void processDT() {
		ProactiveHandler pro = ProactiveHandler.getTheHandler();
		ProactiveResponseHandler respHdlr = ProactiveResponseHandler
				.getTheHandler();

		pro.init(PRO_CMD_PROVIDE_LOCAL_INFORMATION, (byte) 0x00, DEV_ID_ME);
		pro.send();

		pro.init(PRO_CMD_DISPLAY_TEXT, (byte) DT_CQ, DEV_ID_ME);
		pro.appendTLV(TAG_TEXT_STRING, DCS_8_BIT_DATA, msg, OFFSET_ZERO,
				(short) msg.length);
		pro.send();
		/*
		 * proHdlr.initDisplayText( DT_CQ, // qualifier (wait for user, prio
		 * high) DCS_8_BIT_DATA, // dcs msg, OFFSET_ZERO, (short)msg.length );
		 * // length
		 */
	}

}

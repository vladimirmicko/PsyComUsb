package com.vladimir.PsyComUsb;

import java.nio.ByteBuffer;
import java.util.List;

import javax.usb.UsbClaimException;
import javax.usb.UsbConfiguration;
import javax.usb.UsbConst;
import javax.usb.UsbControlIrp;
import javax.usb.UsbDevice;
import javax.usb.UsbDeviceDescriptor;
import javax.usb.UsbDisconnectedException;
import javax.usb.UsbEndpoint;
import javax.usb.UsbException;
import javax.usb.UsbHostManager;
import javax.usb.UsbHub;
import javax.usb.UsbInterface;
import javax.usb.UsbInterfacePolicy;
import javax.usb.UsbNotActiveException;
import javax.usb.UsbPipe;
import javax.usb.UsbServices;

import org.usb4java.DeviceHandle;
import org.usb4java.LibUsb;
import org.usb4java.LibUsbException;
import org.usb4java.Transfer;

public class DrugacijiPrimerKojiNeRadi {

	/** The vendor ID of the ArtMedico */
	private static final short VENDOR_ID = 0x2786;
	/** The product ID of the BioScope BS20 */
	private static final short PRODUCT_ID = 0x2720;

	private UsbDevice device;
	private DeviceHandle devH;

	public static void main(String[] args) {
		DrugacijiPrimerKojiNeRadi app = new DrugacijiPrimerKojiNeRadi();
		app.initialize(app);

	}

	public void initialize(DrugacijiPrimerKojiNeRadi app) {
		
		this.sendData(new byte[] { (byte) 0x73, (byte) 0x73 });
	
	}


    private void sendData(byte[] barrData) throws LibUsbException {
        ByteBuffer cXferSetup = ByteBuffer.allocateDirect(13);
        Transfer xFer = LibUsb.allocTransfer();
        LibUsb.fillControlSetup(
                cXferSetup, (byte)0x21, (byte)0x9,
                (byte)0x200, (byte)0x0, (byte)0x5
        );
        cXferSetup.put(barrData);
        LibUsb.fillControlTransfer(xFer, devH, cXferSetup, null, xFer, 0);
        this.errCheck(
            "Unable to communicate with remote",
            LibUsb.submitTransfer(xFer)
        );
    }


    private void errCheck(String msg, int r) throws LibUsbException{
        if (r < 0) throw new LibUsbException(msg, r);
    }

}

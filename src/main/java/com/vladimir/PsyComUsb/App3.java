package com.vladimir.PsyComUsb;

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

public class App3 {

	/** The vendor ID of the ArtMedico */
	private static final short VENDOR_ID = 0x2786;
	/** The product ID of the BioScope BS20 */
	private static final short PRODUCT_ID = 0x2720;

	private UsbDevice device;

	public static void main(String[] args) {
		App3 app = new App3();
		app.initialize(app);

	}

	public void initialize(App3 app) {
		UsbHub usbHub = null;
		UsbServices services;
		try {
			services = UsbHostManager.getUsbServices();
			usbHub = services.getRootUsbHub();
		} catch (SecurityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UsbException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		device = findDevice(usbHub, VENDOR_ID, PRODUCT_ID);

		if (device == null) {
			// logger.warn("Could not find the device. The driver is not operable.");
			System.out.println("Could not find the device. The driver is not operable.");
			return;
		}

		System.out.println("USB device found!");

		UsbConfiguration configuration = device.getUsbConfiguration((byte) 1);
		UsbInterface iface = configuration.getUsbInterface((byte) 0);
		try {
			iface.claim(new UsbInterfacePolicy() {
				public boolean forceClaim(UsbInterface usbInterface) {
					return true;
				}
			});
		} catch (UsbClaimException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UsbNotActiveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UsbDisconnectedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UsbException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		final byte[] bytes = new byte[64];
		bytes[0] = (byte) 0x73;
		bytes[1] = (byte) 0x00;


//		try {
//			app.sendMessage(device, bytes);
//		} catch (UsbException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		app.sendMessage(iface, bytes, 0x01);
//		app.sendMessage(iface, bytes, 0x81);		
//	    app.readMessage(iface, 0x01);
	}

	public static void sendMessage(UsbDevice device, byte[] message) throws UsbException {
		UsbControlIrp irp = device.createUsbControlIrp(
				(byte) (UsbConst.REQUESTTYPE_TYPE_CLASS | UsbConst.REQUESTTYPE_RECIPIENT_INTERFACE), (byte) 0x09,
				(short) 2, (short) 1);
		irp.setData(message);
		device.syncSubmit(irp);
	}

	public void sendMessage(UsbInterface iface, byte[] bytes, int i) {
		UsbPipe pipe = null;

		try {
			UsbEndpoint endpoint = (UsbEndpoint) iface.getUsbEndpoint((byte) i);
			pipe = endpoint.getUsbPipe();
			pipe.open();

			int sent = pipe.syncSubmit(bytes);

			System.out.println(sent + " bytes sent");
			pipe.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				iface.release();
			} catch (UsbClaimException e) {
				e.printStackTrace();
			} catch (UsbNotActiveException e) {
				e.printStackTrace();
			} catch (UsbDisconnectedException e) {
				e.printStackTrace();
			} catch (UsbException e) {
				e.printStackTrace();
			}
		}
	}

	public UsbDevice findDevice(UsbHub hub, short vendorId, short productId) {
		for (UsbDevice device : (List<UsbDevice>) hub.getAttachedUsbDevices()) {
			UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();
			if (desc.idVendor() == vendorId && desc.idProduct() == productId)
				return device;
			if (device.isUsbHub()) {
				device = findDevice((UsbHub) device, vendorId, productId);
				if (device != null)
					return device;
			}
		}
		return null;
	}

}

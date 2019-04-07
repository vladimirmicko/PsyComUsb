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
import javax.usb.UsbIrp;
import javax.usb.UsbNotActiveException;
import javax.usb.UsbNotClaimedException;
import javax.usb.UsbNotOpenException;
import javax.usb.UsbPipe;
import javax.usb.UsbServices;
import javax.usb.util.DefaultUsbIrp;

public class BioScope {

	/** The vendor ID of the ArtMedico */
	private static final short VENDOR_ID = 0x2786;
	/** The product ID of the BioScope BS20 */
	private static final short PRODUCT_ID = 0x2720;

	private UsbDevice device;
	private UsbInterface iface;

	public static void main(String[] args) {
		BioScope app = new BioScope();
		app.initialize(app);

	}

	public void initialize(BioScope app) {
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
		iface = configuration.getUsbInterface((byte) 0);

		
		try {
			iface.claim(new UsbInterfacePolicy() {
				public boolean forceClaim(UsbInterface usbInterface) {
					return true;
				}
			});
			
			byte[] bytes = new byte[64];
			readCurrentConfiguration();

			bytes = new byte[64];
			bytes[0] = (byte) 0x73;
			bytes[1] = (byte) 0x00;
			app.sendMessage(bytes);
			
			bytes = new byte[64];
			bytes[0] = (byte) 0x88;
			bytes[1] = (byte) 0x00;
			app.sendMessage(bytes);
			app.readUSB();
			
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
		finally {
			try {
				iface.release();
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
		}
	}


	public void sendMessage(byte[] bytes) {
		UsbPipe pipe = null;

		try {
			UsbEndpoint endpoint = (UsbEndpoint) iface.getUsbEndpoint((byte) 0x01);
			pipe = endpoint.getUsbPipe();
			pipe.open();

			int sent = pipe.syncSubmit(bytes);

			System.out.println(sent + " bytes sent");
//			pipe.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				pipe.close();
//				iface.release();
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

	public void readUSB() {

		UsbEndpoint endpoint = iface.getUsbEndpoint((byte) 0x81);
		UsbPipe pipe = endpoint.getUsbPipe();
		
		int maxPacketSize = endpoint.getUsbEndpointDescriptor().wMaxPacketSize( );
		byte[] buffer = new byte[64];
		UsbIrp irp1 = pipe.createUsbIrp( );
		irp1.setData(buffer);
		irp1.setOffset(0);
		irp1.setLength(maxPacketSize);


		try {
			pipe.open();
			int received = pipe.syncSubmit(buffer);
			System.out.println(received + " bytes received");
			System.out.println("buffer[0]" + buffer[0]);
			System.out.println("buffer[1]" + buffer[1]);
			System.out.println("buffer[2]" + buffer[2]);
			System.out.println("buffer[3]" + buffer[3]);
			System.out.println("buffer[4]" + buffer[4]);
			System.out.println("buffer[5]" + buffer[5]);
			System.out.println("buffer[6]" + buffer[6]);
			System.out.println("buffer[7]" + buffer[7]);
			System.out.println("buffer[8]" + buffer[8]);
			System.out.println("buffer[9]" + buffer[9]);
			
			
		} catch (UsbNotActiveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UsbNotClaimedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UsbDisconnectedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UsbException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				pipe.close();
			} catch (UsbNotActiveException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UsbNotOpenException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UsbDisconnectedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UsbException e) {
				// TODO Auto-generated catch block
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

	public void readCurrentConfiguration() {
		UsbControlIrp irp = device.createUsbControlIrp(
				(byte) (UsbConst.REQUESTTYPE_DIRECTION_IN | UsbConst.REQUESTTYPE_TYPE_STANDARD
						| UsbConst.REQUESTTYPE_RECIPIENT_DEVICE),
				UsbConst.REQUEST_GET_CONFIGURATION, (short) 0, (short) 0);
		irp.setData(new byte[1]);
		try {
			device.syncSubmit(irp);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UsbDisconnectedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UsbException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(irp.getData()[0]);
	}
}

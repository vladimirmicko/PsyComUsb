package com.vladimir.PsyComUsb;

import java.util.List;

import javax.usb.UsbConst;
import javax.usb.UsbDevice;
import javax.usb.UsbDeviceDescriptor;
import javax.usb.UsbEndpoint;
import javax.usb.UsbException;
import javax.usb.UsbHostManager;
import javax.usb.UsbHub;
import javax.usb.UsbInterface;
import javax.usb.UsbServices;

public class App {
	
	/** The vendor ID of the led stripe. */
	private static final short VENDOR_ID = 0x2786;

	/** The product ID of the led stripe. */
	private static final short PRODUCT_ID = 0x7750;

	public static void main(String[] args) {
		App app = new App();
		try {
			app.initialize();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (UsbException e) {
			e.printStackTrace();
		}

	}
	
	
	public UsbDevice findDevice(UsbHub hub, short vendorId, short productId)
	{
	    for (UsbDevice device : (List<UsbDevice>) hub.getAttachedUsbDevices())
	    {
	        UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();
	        if (desc.idVendor() == vendorId && desc.idProduct() == productId) return device;
	        if (device.isUsbHub())
	        {
	            device = findDevice((UsbHub) device, vendorId, productId);
	            if (device != null) return device;
	        }
	    }
	    return null;
	}
	
	protected void initialize() throws SecurityException, UsbException {
		UsbServices services = UsbHostManager.getUsbServices();
		UsbHub usbHub = services.getRootUsbHub();

		UsbDevice theDevice = findDevice(usbHub, VENDOR_ID, PRODUCT_ID);

		if (theDevice == null) {
			//logger.warn("Could not find the device. The driver is not operable.");
			return;
		}
		
		for (Object i : theDevice.getActiveUsbConfiguration().getUsbInterfaces()) {
			UsbInterface intf = (UsbInterface) i;
			for (Object e : intf.getUsbEndpoints()) {
				UsbEndpoint endp = (UsbEndpoint) e;
				if (endp.getDirection() == UsbConst.ENDPOINT_DIRECTION_IN) {
					//this.pipe = endp.getUsbPipe();
				}
			}
		}
	}
	
	

}

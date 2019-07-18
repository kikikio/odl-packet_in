package org.opendaylight.defender.impl;

import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.text.SimpleDateFormat;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import java.io.*;


public class PacketHandler implements PacketProcessingListener
{
	//private NotificationPublishService notificationPublishService;
    private static final Logger LOG = LoggerFactory.getLogger(PacketHandler.class);

	public PacketHandler()
	{
	    counter = 0;
		LOG.info("[liuhy] PacketHandler Initiated. ");
	}

    String srcIP, dstIP, ipProtocol, srcMac, dstMac;
    String stringEthType;
    int srcPort, dstPort;
    int counter;
    byte[] payload, srcMacRaw, dstMacRaw, srcIPRaw, dstIPRaw, rawIPProtocol, rawEthType, rawSrcPort, rawDstPort;


    NodeConnectorRef ingressNodeConnectorRef;
    // Ingress Switch Id
    NodeId ingressNodeId;
    // Ingress Switch Port Id from DataStore
    NodeConnectorId ingressNodeConnectorId;
    String ingressConnector, ingressNode;


	@Override
    public void onPacketReceived(PacketReceived notification)
    {
        // TODO Auto-generated method stub

        //LOG.info("[liuhy] enter 1 !!!!!");
        ingressNodeConnectorRef = notification.getIngress();
        ingressNodeConnectorId = InventoryUtility.getNodeConnectorId(ingressNodeConnectorRef);
        ingressConnector = ingressNodeConnectorId.getValue();
        ingressNodeId = InventoryUtility.getNodeId(ingressNodeConnectorRef);
        ingressNode = ingressNodeId.getValue();
        //LOG.info("[liuhy] enter 2 !!!!!");

        //LOG.info("[liuhy] ingressNode " + ingressNode);

        //packetSize = payload.length;

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DATE);
        int hour = cal.get(Calendar.HOUR);
        int minute = cal.get(Calendar.MINUTE);
        int second = cal.get(Calendar.SECOND);
        String div1 = ".";
        String div2 = ":";
        String time = String.format("%04d", year) + div1 + String.format("%02d", month) + div1 + String.format("%02d", day) + "-" + String.format("%02d", hour) + div2 + String.format("%02d", minute) + div2 + String.format("%02d", second);




        payload = notification.getPayload();
        //LOG.info("[liuhy] enter 3 !!!!!");
        int packetSize = payload.length;
        srcMacRaw = PacketParsing.extractSrcMac(payload);
        dstMacRaw = PacketParsing.extractDstMac(payload);
        srcMac = PacketParsing.rawMacToString(srcMacRaw);
        dstMac = PacketParsing.rawMacToString(dstMacRaw);

        rawEthType = PacketParsing.extractEtherType(payload);
        stringEthType = PacketParsing.rawEthTypeToString(rawEthType);

        if (dstMac.equals("FF:FF:FF:FF:FF:FF") && stringEthType.equals("806"))
        {
            LOG.info("[liuhy] This is an ARP packet ");
            LOG.info("[liuhy] Received packet from MAC {} to MAC {}, EtherType=0x{} ", srcMac, dstMac, stringEthType);
        }
        else if (stringEthType.equals("800"))
        {
            dstIPRaw = PacketParsing.extractDstIP(payload);
            srcIPRaw = PacketParsing.extractSrcIP(payload);
            dstIP = PacketParsing.rawIPToString(dstIPRaw);
            srcIP = PacketParsing.rawIPToString(srcIPRaw);

            rawIPProtocol = PacketParsing.extractIPProtocol(payload);
            ipProtocol = PacketParsing.rawIPProtoToString(rawIPProtocol).toString();

            rawSrcPort = PacketParsing.extractSrcPort(payload);
            srcPort = PacketParsing.rawPortToInteger(rawSrcPort);
            rawDstPort = PacketParsing.extractDstPort(payload);
            dstPort = PacketParsing.rawPortToInteger(rawDstPort);



            String content = "Time " + time + " src_IP " + srcIP + " dst_IP " + dstIP + " EtherType 0x0" + stringEthType + " srcProt " + srcPort + " dstPort " + dstPort + " size " + String.valueOf(packetSize);
            String path = "/home/ovs/" + ingressNode + "_pktin.txt";

            FileWriter fw = new FileWriter(path);
            fw.writeLine(content);


            //LOG.info("[liuhy] Received packet from IP {} to IP {}, EtherType=0x{} ", srcIP, dstIP, stringEthType);

        }

        counter = counter + 1;
        LOG.info("[liuhy] Totally receive {} packets for now ", counter);
    }

	
}
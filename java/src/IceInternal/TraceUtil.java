// **********************************************************************
//
// Copyright (c) 2001
// MutableRealms, Inc.
// Huntsville, AL, USA
//
// All Rights Reserved
//
// **********************************************************************

package IceInternal;

final class TraceUtil
{
    static void
    traceHeader(String heading, BasicStream str, Ice.Logger logger,
                TraceLevels tl)
    {
        if (tl.protocol >= 1)
        {
            BasicStream stream = new BasicStream(str);
            java.io.StringWriter s = new java.io.StringWriter();
            s.write(heading);
            printHeader(s, stream);
            logger.trace(tl.protocolCat, s.toString());
        }
    }

    static void
    traceRequest(String heading, BasicStream str, Ice.Logger logger,
                 TraceLevels tl)
    {
        if (tl.protocol >= 1)
        {
            BasicStream stream = new BasicStream(str);
            java.io.StringWriter s = new java.io.StringWriter();
            s.write(heading);
            printHeader(s, stream);
            int requestId = stream.readInt();
            s.write("\nrequest id = " + requestId);
            if (requestId == 0)
            {
                s.write(" (oneway)");
            }
            printRequestHeader(s, stream);
            logger.trace(tl.protocolCat, s.toString());
        }
    }

    static void
    traceBatchRequest(String heading, BasicStream str, Ice.Logger logger,
                      TraceLevels tl)
    {
        if (tl.protocol >= 1)
        {
            BasicStream stream = new BasicStream(str);
            java.io.StringWriter s = new java.io.StringWriter();
            s.write(heading);
            printHeader(s, stream);
            int cnt = 0;
            while (stream.pos() != stream.size())
            {
                s.write("\nrequest #" + cnt + ':');
                cnt++;
                printRequestHeader(s, stream);
                stream.skipEncaps();
            }
            logger.trace(tl.protocolCat, s.toString());
        }
    }

    static void
    traceReply(String heading, BasicStream str, Ice.Logger logger,
               TraceLevels tl)
    {
        if (tl.protocol >= 1)
        {
            BasicStream stream = new BasicStream(str);
            java.io.StringWriter s = new java.io.StringWriter();
            s.write(heading);
            printHeader(s, stream);
            int requestId = stream.readInt();
            s.write("\nrequest id = " + requestId);
            byte status = stream.readByte();
            s.write("\nreply status = " + (int)status + ' ');
            switch (status)
            {
                case DispatchStatus._DispatchOK:
                {
                    s.write("(ok)");
                    break;
                }
                case DispatchStatus._DispatchUserException:
                {
                    s.write("(user exception)");
                    break;
                }
                case DispatchStatus._DispatchLocationForward:
                {
                    s.write("(location forward)");
                    break;
                }
                case DispatchStatus._DispatchObjectNotExist:
                {
                    s.write("(object not exist)");
                    break;
                }
                case DispatchStatus._DispatchOperationNotExist:
                {
                    s.write("(operation not exist)");
                    break;
                }
                case DispatchStatus._DispatchUnknownLocalException:
                {
                    s.write("(unknown local exception)");
                    break;
                }
                case DispatchStatus._DispatchUnknownException:
                {
                    s.write("(unknown exception)");
                    break;
                }
                default:
                {
                    s.write("(unknown)");
                    break;
                }
            }
            logger.trace(tl.protocolCat, s.toString());
        }
    }

    public static void
    dumpStream(BasicStream stream)
    {
        final int inc = 8;

        int pos = stream.pos();
        stream.pos(0);

        byte[] data = stream.readBlob(stream.size());
        dumpOctets(data);

        stream.pos(pos);
    }

    public static void
    dumpOctets(byte[] data)
    {
        final int inc = 8;

        for(int i = 0; i < data.length; i += inc)
        {
            for(int j = i ; j - i < inc ; j++)
            {
                if(j < data.length)
                {
                    int n = (int)data[j];
                    if(n < 0)
                        n += 256;
                    String s;
                    if(n < 10)
                        s = "  " + n;
                    else if(n < 100)
                        s = " " + n;
                    else
                        s = "" + n;
                    System.out.print(s + " ");
                }
                else
                    System.out.print("    ");
            }

            System.out.print('"');

            for(int j = i; j < data.length && j - i < inc; j++)
            {
                if(data[j] >= (byte)32 && data[j] < (byte)127)
                    System.out.print((char)data[j]);
                else
                    System.out.print('.');
            }

            System.out.println('"');
        }
    }

    private static void
    printHeader(java.io.Writer out, BasicStream stream)
    {
        try
        {
            byte protVer = stream.readByte();
            out.write("\nprotocol version = " + (int)protVer);
            byte encVer = stream.readByte();
            out.write("\nencoding version = " + (int)encVer);
            byte type = stream.readByte();
            out.write("\nmessage type = " + (int)type + ' ');
            switch (type)
            {
                case Protocol.requestMsg:
                {
                    out.write("(request)");
                    break;
                }
                case Protocol.requestBatchMsg:
                {
                    out.write("(batch request)");
                    break;
                }
                case Protocol.replyMsg:
                {
                    out.write("(reply)");
                    break;
                }
                case Protocol.closeConnectionMsg:
                {
                    out.write("(close connection)");
                    break;
                }
                default:
                {
                    out.write("(unknown)");
                    break;
                }
            }
            int size = stream.readInt();
            out.write("\nmessage size = " + size);
        }
        catch (java.io.IOException ex)
        {
            assert(false);
        }
    }

    private static void
    printRequestHeader(java.io.Writer out, BasicStream stream)
    {
        try
        {
            Ice.Identity identity = new Ice.Identity();
            identity.__read(stream);
            out.write("\nidentity = " + Ice.Util.identityToString(identity));
            String facet = stream.readString();
            out.write("\nfacet = " + facet);
            String operation = stream.readString();
            out.write("\noperation = " + operation);
            boolean nonmutating = stream.readBool();
            out.write("\nnonmutating = " + nonmutating);
            int sz = stream.readInt();
            out.write("\ncontext = ");
            while (sz-- > 0)
            {
                String key = stream.readString();
                String value = stream.readString();
                out.write(key + '/'+ value);
                if (sz > 0)
                {
                    out.write(", ");
                }
            }
        }
        catch (java.io.IOException ex)
        {
            assert(false);
        }
    }
}

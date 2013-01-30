<%@ page import="java.util.Date" %>
<%@ page import="jhole.server.ClientManager" %>
<%@ page import="jhole.server.Client" %>
<%@ page import="java.util.Map" %>
<%@ page import="jhole.server.MessageDrivenClient" %>
<%@ page import="jhole.messaging.direct.DeliveringMessenger" %>
<%@ page import="jhole.connector.Peer" %>
<%@ page import="jhole.connector.direct.SocketPeer" %>
<%@ page import="java.util.TreeMap" %>
<%@ page import="jhole.utils.TimeUtils" %>
<html>
<head>

    <style type="text/css">
        table {
            border-collapse: collapse;
        }

        td, th {
            border: solid 1px #CCC;
        }

        table form {
            display: inline;
        }

        ul.error {
            border: red 3px solid;
            font-weight: bold;
        }

        .weak {
            padding: 0 10px;
            margin: 0;
            color: #CCC;
        }

        td label {
            margin: 0;
        }

        td {
            padding: 0 5px;
        }

        a.selected {
            font-weight: bold;
            background: #e1e1e1;
            padding: 0 5px;
        }
    </style>
</head>
<body>
    <%
        ClientManager clientManager = (ClientManager) application.getAttribute("clientManager");

        if (request.getParameter("close") != null) {
            MessageDrivenClient client = (MessageDrivenClient) clientManager.get(request.getParameter("client"));
            DeliveringMessenger messenger = (DeliveringMessenger) client.getMessenger();
            String connection = request.getParameter("connection");
            if (connection != null) {
                messenger.getConnections().get(Long.valueOf(connection)).disconnect();
            } else {
                client.destroy();
            }

            response.sendRedirect(request.getParameter("redirectTo"));
            return;
        }

    %>
    <%
        for (Map.Entry<String, Client> entry: clientManager.getClients().entrySet()) {
            MessageDrivenClient client = (MessageDrivenClient) entry.getValue();
            String id = entry.getKey();
            DeliveringMessenger messenger = (DeliveringMessenger) client.getMessenger();
    %>
    <ul>
        <li>ID: <%=id%></li>
        <li>Queue Size: <%=client.getQueueSize()%></li>
        <li>Life: <%=TimeUtils.formatInterval(System.currentTimeMillis() - client.getCreationTime())%></li>
        <li>
            <a href="<%=application.getContextPath()%>/?client=<%=id%>&close&redirectTo=<%=request.getRequestURL()%>">close</a>
        </li>
    </ul>
    <table>
        <tr>
            <th>ID</th>
            <th>Destination</th>
            <th>State</th>
            <th>Last Used</th>
            <th>Read/Written</th>
            <th></th>
        </tr>
        <%
            for (Map.Entry<Long, Peer> peerEntry: new TreeMap<Long,Peer>(messenger.getConnections()).entrySet()) {
                SocketPeer peer = (SocketPeer) peerEntry.getValue();
        %>
        <tr>
            <td><%=peerEntry.getKey()%></td>
            <td><%=peer.getSocket().getRemoteSocketAddress()%></td>
            <td><%=peer.getSocket().isClosed() ? "Closed" : "Opened" %></td>
            <td><%=TimeUtils.formatInterval(System.currentTimeMillis() - peer.getOpenedTime())%></td>
            <td><%=peer.getReadBytes()%>/<%=peer.getWrittenBytes()%></td>
            <td>
                <a href="<%=application.getContextPath()%>/?client=<%=id%>&connection=<%=peerEntry.getKey()%>&close&redirectTo=<%=request.getRequestURL()%>">close</a>
            </td>
        </tr>
        <%
            }
        %>
    </table>
    <%
        }
    %>
</body>
</html>
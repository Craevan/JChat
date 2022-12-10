[![Codacy Badge](https://app.codacy.com/project/badge/Grade/4c05291b9ff74733a5fa2bc2c36d0b32)](https://www.codacy.com/gh/Craevan/JChat/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=Craevan/JChat&amp;utm_campaign=Badge_Grade)
___

### JChatServer

* Server.java - main server class
* MessageType.java - Enum, messages type description client and server exchange with
* Message.java - forwarded messages class
* Connection.java - client and server connection description

___

### JChatClient

* Client.java - main client class. May be used as CLI client as well

The graphical user interface is built using Java Swing and implements the MVC pattern.

* ClientGuiController.java - extends Client. Main class for GUI client
* ClientGuiModel.java - Model class
* ClientGuiView.java - View component

___

### How to

1. Download binary distribution
2. To start the server type in terminal `java -jar {your_path}/JServer.jar`
3. To launch the client just double-click on JChatClient.jar
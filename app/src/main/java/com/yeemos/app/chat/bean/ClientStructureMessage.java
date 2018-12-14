package com.yeemos.app.chat.bean;

public class ClientStructureMessage {
	private TextMessage textMessage = null;
	private AudioMessage audioMessage = null;
	private VideoMessage videoMessage = null;
	private ImageMessage imageMessage = null;
	private ReceivedRead receivedRead = null;
	private PeerRead peerRead = null;
	private PeerRecieved peerRecieved = null;
	private UserOffline userOffline = null;
	private UserOnline userOnline = null;
	private ServerConfirm serverConfirm = null;
	private FileURLMessage fileURLMessage = null;
	private UserStartTyping userStartTyping = null;
	private UserStopTyping userStopTyping = null;
	private BroadcastMessage broadcastMessage = null;


	public TextMessage getTextMessage() {
		return textMessage;
	}

	public void setTextMessage(TextMessage textMessage) {
		this.textMessage = textMessage;
	}

	public AudioMessage getAudioMessage() {
		return audioMessage;
	}

	public void setAudioMessage(AudioMessage audioMessage) {
		this.audioMessage = audioMessage;
	}

	public VideoMessage getVideoMessage() {
		return videoMessage;
	}

	public void setVideoMessage(VideoMessage videoMessage) {
		this.videoMessage = videoMessage;
	}

	public ImageMessage getImageMessage() {
		return imageMessage;
	}

	public void setImageMessage(ImageMessage imageMessage) {
		this.imageMessage = imageMessage;
	}

	public PeerRead getPeerRead() {
		return peerRead;
	}

	public void setPeerRead(PeerRead peerRead) {
		this.peerRead = peerRead;
	}

	public ReceivedRead getReceivedRead() {
		return receivedRead;
	}

	public void setReceivedRead(ReceivedRead receivedRead) {
		this.receivedRead = receivedRead;
	}

	public PeerRecieved getPeerRecieved() {
		return peerRecieved;
	}

	public void setPeerRecieved(PeerRecieved peerRecieved) {
		this.peerRecieved = peerRecieved;
	}

	public UserOffline getUserOffline() {
		return userOffline;
	}

	public void setUserOffline(UserOffline userOffline) {
		this.userOffline = userOffline;
	}

	public UserOnline getUserOnline() {
		return userOnline;
	}

	public void setUserOnline(UserOnline userOnline) {
		this.userOnline = userOnline;
	}



	public ServerConfirm getServerConfirm() {
		return serverConfirm;
	}

	public void setServerConfirm(ServerConfirm serverConfirm) {
		this.serverConfirm = serverConfirm;
	}


	public FileURLMessage getFileURLMessage() {
		return fileURLMessage;
	}

	public void setFileURLMessage(FileURLMessage fileURLMessage) {
		this.fileURLMessage = fileURLMessage;
	}


	public UserStartTyping getUserStartTyping() {
		return userStartTyping;
	}

	public void setUserStartTyping(UserStartTyping userStartTyping) {
		this.userStartTyping = userStartTyping;
	}

	public UserStopTyping getUserStopTyping() {
		return userStopTyping;
	}

	public void setUserStopTyping(UserStopTyping userStopTyping) {
		this.userStopTyping = userStopTyping;
	}

	public BroadcastMessage getBroadcastMessage() {
		return broadcastMessage;
	}

	public void setBroadcastMessage(BroadcastMessage broadcastMessage) {
		this.broadcastMessage = broadcastMessage;
	}
}

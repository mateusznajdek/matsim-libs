package org.matsim.core.mobsim.qsim.communication.service.worker.sync;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.matsim.core.mobsim.qsim.communication.model.messages.SyncStepMessage;

import java.util.concurrent.BlockingQueue;

public class IncomingMessageBuffer {
	private final static Logger LOG = LogManager.getLogger(IncomingMessageBuffer.class);
	private final BlockingQueue<SyncStepMessage> incomingMessages;
	private final BlockingQueue<SyncStepMessage> futureIncomingMessages;
	private int consumedMessages;

	public IncomingMessageBuffer(BlockingQueue<SyncStepMessage> incomingMessages, BlockingQueue<SyncStepMessage> futureIncomingMessages) {
		this.incomingMessages = incomingMessages;
		this.futureIncomingMessages = futureIncomingMessages;
	}

	private int currentStep = 0;

	public synchronized int processNewMsg(int countOfNeighbours) {
		consumedMessages++;
		if (consumedMessages == countOfNeighbours) {
			currentStep++;
			incomingMessages.clear();
			if (!futureIncomingMessages.isEmpty()) {
				LOG.trace("Draining futureIncomingMessages" +
					", currentStep: " + currentStep +
					", consumedMessages: " + consumedMessages +
					", futureIncomingMessages: " + futureIncomingMessages.peek());
				while (!futureIncomingMessages.isEmpty() && futureIncomingMessages.peek().getStep() == currentStep) {
					incomingMessages.add(futureIncomingMessages.poll());
				}
			}
		}
		return 0;
	}

	public synchronized void give(SyncStepMessage syncStepMessage) {
		LOG.debug(Thread.currentThread() + "::: SyncStepMessage from w: " + syncStepMessage.getWorkerId() +
			", random: " + syncStepMessage.getRandom()
			+ ", step: " + syncStepMessage.getStep()
			+ ", incomingMessages: " + incomingMessages.size()
			+ ", futureIM: " + futureIncomingMessages.size());
		if (syncStepMessage.getStep() > currentStep) {
			futureIncomingMessages.add(syncStepMessage);
		} else if (syncStepMessage.getStep() == currentStep) {
			incomingMessages.add(syncStepMessage);
			// notifyAll();
		} else {
			throw new RuntimeException("Received timestep from the past - impossible");
		}
	}

	public synchronized int getConsumedMessages() {
		return consumedMessages;
	}

	public synchronized void initConsumedMessages() {
		this.consumedMessages = 0;
	}
}



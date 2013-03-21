package com.github.dnsev.videncode;



public abstract class ManagedThread extends Thread {
	private ThreadManager manager;

	public ManagedThread(final ThreadManager manager) {
		super();

		this.manager = manager;
	}

	@Override
	public final void start() {
		this.manager.queueThreadForCleanup(this);
		super.start();
	}

	protected abstract void execute();

	public void stopRunning() {
	}

	@Override
	public final void run() {
		this.execute();
		this.completed();
	}

	private final void completed() {
		this.manager.unqueueThreadForCleanup(this);
	}
}



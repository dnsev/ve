package com.github.dnsev.videncode;

import java.util.ArrayList;



public abstract class ThreadManager {
	private ArrayList<ManagedThread> cleanupThreads = new ArrayList<ManagedThread>();

	public ThreadManager() {
	
	}

	public final void cleanThreads() {
		// Get a copy of all the threads that need joining
		ArrayList<ManagedThread> cleanupThreadsCopy;
		synchronized (this.cleanupThreads) {
			cleanupThreadsCopy = new ArrayList<ManagedThread>(this.cleanupThreads);
		}

		// Join them all
		for (int i = 0; i < cleanupThreadsCopy.size(); ++i) {
			try {
				cleanupThreadsCopy.get(i).stopRunning();
				cleanupThreadsCopy.get(i).join();
			}
			catch (InterruptedException e) {}
		}
	}

	public final void queueThreadForCleanup(ManagedThread t) {
		synchronized (this.cleanupThreads) {
			this.cleanupThreads.add(t);
		}
	}
	public final void unqueueThreadForCleanup(ManagedThread t) {
		synchronized (this.cleanupThreads) {
			this.cleanupThreads.remove(t);
		}
	}

}



package com.test;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PrintTest {
	static Lock lock = new ReentrantLock();
	static Condition[] conditions = new Condition[5];
	static int turn = 0;
	public static void main(String[] args) {
		
		for(int i=0;i<conditions.length;i++) {
			conditions[i] = lock.newCondition();
		}
		Thread threadA = new Thread(new PrintThread(0, "A"));
		Thread threadB = new Thread(new PrintThread(1, "B"));
		Thread threadC = new Thread(new PrintThread(2, "C"));
		Thread threadD = new Thread(new PrintThread(3, "D"));
		Thread threadE = new Thread(new PrintThread(4, "E"));
		threadA.start();
		threadB.start();
		threadC.start();
		threadD.start();
		threadE.start();
	}
	
	public static void  doPrintWork(int threadnum,String str) throws InterruptedException {
		lock.lock();
		try {
			while(threadnum != turn) {
				conditions[threadnum].await();
			}
			System.out.println("now I am printing " + str);
			turn = (turn + 1) % 5;
			conditions[turn].signal();
		}finally {
			lock.unlock();
		}
		
	}
	
	private static  class PrintThread implements Runnable{
		private int threadnum;
		private String str;
		

		public PrintThread(int threadnum, String str) {
			this.threadnum = threadnum;
			this.str = str;
		}



		@Override
		public void run() {
			for(int i=0;i<10;i++) {
				try {
					doPrintWork(threadnum,str);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
		
	}
	

}

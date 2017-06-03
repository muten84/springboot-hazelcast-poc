package it.luigibifulco.distributedlock.controller;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;

import it.luigibifulco.distributedlock.entity.Product;
import it.luigibifulco.distributedlock.service.ProductService;

@RestController
public class LockController {

	@Autowired
	private HazelcastInstance h;

	@Autowired
	private ProductService service;

	AtomicInteger atomicInt = new AtomicInteger(0);

	@RequestMapping(value = "/insertWithLock")
	public String withLock() {
		return insert(1);
	}

	@RequestMapping(value = "/insertWithoutLock")
	public String withoutLock() {
		return insertWithoutLock();
	}

	@RequestMapping(value = "/lockreset")
	public boolean resetSequence() {
		atomicInt.set(0);
		return true;
	}

	@RequestMapping(value = "/getall")
	public List<String> getall() {
		return service.listAllProducts();
	}

	@RequestMapping(value = "/deleteall")
	public long deleteall() {
		return service.deleteAll();
	}

	private String insertWithoutLock() {
		Product p = service.insertProduct();
		if (p == null) {
			return "error";
		}
		return p.getSeq().toString();
	}

	private String insert(int amount) {
		Product p = null;
		ILock l = h.getLock("savelock");
		try {
			if (l.tryLock(10, TimeUnit.SECONDS)) {
				int val = atomicInt.accumulateAndGet(amount, LockController::sum);
				p = service.insertProduct();
			}
			// Thread.sleep(300);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				l.unlock();
			} catch (Exception e) {
				l.forceUnlock();
			}

		}
		if (p == null) {
			return "error";
		}
		return p.getSeq().toString();
	}

	private static int sum(int x, int y) {
		return x + y;
	}

}

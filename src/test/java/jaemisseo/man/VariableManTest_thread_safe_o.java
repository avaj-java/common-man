package jaemisseo.man;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;

public class VariableManTest_thread_safe_o {
	static final int poolSize = 50;
	static final int count = 5000;
	int globalIndex = -1;
	private VariableMan newFormat() {
		return new VariableMan();
	}
	private Callable<String> makeThreadSafeTask(){
		Callable<String> task = new Callable<String>() {
			public String call() throws Exception {
				Integer index = (++globalIndex);
				String threadName = Thread.currentThread().getName();
				System.out.println("threadName ==> " + threadName);

				HashMap map = new HashMap<String, Integer>();
				map.put("abc", index);
				map.put("abc1", "ㅁ나얼미ㅏㄴㅇ럼니ㅏㅇ런미ㅏㄹ머니;ㅏ런ㅁ퍄ㅐㅡㄷㅈㄱ4ㅐㄱ8ㅐㄱ;ㅡ28ㄱㅍ2갶ㅈㄹ");
				map.put("abc2", "asdfasfja;lefjaeiorja,vajvvwobm8wvamvamhskldfdsflkfjlfksf");
				map.put("abc3", "asdflkajsflkajsf");
				map.put("abc4", 1394394839L);
				return newFormat().parseString(index.toString()+"=${abc}", map);
			}
		};
		return task;
	}



	@Test
	public void thread_safe() throws IllegalThreadStateException {
		// init
		ExecutorService exec = Executors.newFixedThreadPool(poolSize);
		List<Future<String>> results = new ArrayList<Future<String>>();

		try {
			// SimpleDateFormat을 이용한 parse 작업 (멀티 쓰레드)
			Callable<String> task = makeThreadSafeTask();

			// Count만큼 수행
			for (int i=0; i<count; i++) {
				results.add(exec.submit(task));
			}

			// 결과 출력
			for (Future<String> result : results) {
				String answer = result.get();
				String[] arr = answer.split("=");
				boolean same = arr[0].equals(arr[1]);
				System.out.println(answer+ "  ==> " +same);
				if (!same)
					throw new IllegalThreadStateException();
			}

		} catch (IllegalThreadStateException it) {
			it.printStackTrace();
			throw it;
		} catch (ExecutionException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			exec.shutdown();
		}
	}



}
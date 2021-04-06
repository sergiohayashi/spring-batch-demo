package com.example.demo.commons;

import java.util.Arrays;
import java.util.List;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.batch.repeat.RepeatStatus;

public class CommonStepBuilder {
	
	private static class DataSource {
	    List<Integer> values= Arrays.asList( 1, 2, 3, 4, 5); 
		int j[]= new int[] {0};

		private static DataSource _instance= new DataSource();
		
		public synchronized Integer getNext() {
			if( j[0]>= values.size()) return null;
			
			Integer ret= values.get( j[0]++);
			return ret;
		}
		
		public static DataSource getSingleton() {
			return _instance;
		}
	}
	

    public static ItemWriter<Integer> writer() {
		return new ItemWriter<Integer>() {

			@Override
			public void write(List<? extends Integer> items) throws Exception {
				items.forEach( d-> System.out.println( String.format( "%s write: %d", Thread.currentThread().getName(), d)));
			}
		};
	}

	public static ItemReader<Integer> reader() {
	    List<Integer> values= Arrays.asList( 1, 2, 3, 4, 5); 
		int j[]= new int[] {0};

		return new ItemReader<Integer>() {

			@Override
			public Integer read()
					throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
				if( j[0]>= values.size()) return null;
				
				Integer ret= values.get( j[0]++);
				System.out.println( String.format( "%s read: %d", Thread.currentThread().getName(), ret));
				return ret;
			}
	    };
	}	

	
	public static ItemReader<Integer> synchronizedReader() {
		return new ItemReader<Integer>() {

			@Override
			public Integer read()
					throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
				
				Integer ret= DataSource.getSingleton().getNext();
				System.out.println( String.format( "%s read: %s", 
						Thread.currentThread().getName(),
						ret==null?"null":ret.toString()));
				return ret;
			}
	    };
	}	
	
	
	
	public static Tasklet simplePrintTask( String msg) {
		return new Tasklet() {

			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
				System.out.println( String.format( "%s Task - %s", Thread.currentThread().getName(), msg));
				return RepeatStatus.FINISHED;
			}
		};
	}

	public static Tasklet simpleExceptionTask( String msg) {
		return new Tasklet() {

			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
				System.out.println( String.format( "%s Task com erro - %s", Thread.currentThread().getName(), msg));
				throw new IllegalStateException( "Erro simulado para task "+ msg);
			}
		};
	}
	
	
	public static ItemProcessor<Integer,Integer> processor() {
		return new ItemProcessor<Integer,Integer>() {

			@Override
			public Integer process(Integer item) throws Exception {
				System.out.println( String.format( "%s Processor - %d => %d", Thread.currentThread().getName(), item, item+1));
				return item+1;
			}
			
		};
	}
	
	
}

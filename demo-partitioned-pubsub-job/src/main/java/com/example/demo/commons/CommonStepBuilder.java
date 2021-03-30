package com.example.demo.commons;

import java.util.Arrays;
import java.util.List;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

public class CommonStepBuilder {

	
    public static ItemWriter<String> writer() {
		return new ItemWriter<String>() {

			@Override
			public void write(List<? extends String> items) throws Exception {
				items.forEach( d-> System.out.println( String.format( "[%s] WRITE: %s", Thread.currentThread().getName(), d)));
			}
		};
	}
    
    
    //XXX: Nao funciona para multithread...
	public static ItemReader<String> reader() {
	    List<Integer> values= Arrays.asList( 1, 2, 3, 4, 5); 
		int j[]= new int[] {0};

		return new ItemReader<String>() {

			@Override
			public String read()
					throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
				if( j[0]>= values.size()) return null;
				
				var ret= values.get( j[0]++).toString();
				System.out.println( String.format( "%s read: %s", Thread.currentThread().getName(), ret));
				return ret;
			}
	    };
	}	    
	
}

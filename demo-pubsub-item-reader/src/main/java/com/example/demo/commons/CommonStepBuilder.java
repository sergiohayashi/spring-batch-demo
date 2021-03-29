package com.example.demo.commons;

import java.util.List;

import org.springframework.batch.item.ItemWriter;

public class CommonStepBuilder {

	
    public static ItemWriter<String> writer() {
		return new ItemWriter<String>() {

			@Override
			public void write(List<? extends String> items) throws Exception {
				items.forEach( d-> System.out.println( String.format( "[%s] WRITE: %s", Thread.currentThread().getName(), d)));
			}
		};
	}
}

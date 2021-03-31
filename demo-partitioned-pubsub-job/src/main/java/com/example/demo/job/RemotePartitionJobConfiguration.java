package com.example.demo.job;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.deployer.resource.support.DelegatingResourceLoader;
import org.springframework.cloud.deployer.spi.task.TaskLauncher;
import org.springframework.cloud.task.batch.partition.DeployerPartitionHandler;
import org.springframework.cloud.task.batch.partition.DeployerStepExecutionHandler;
import org.springframework.cloud.task.batch.partition.PassThroughCommandLineArgsProvider;
import org.springframework.cloud.task.batch.partition.SimpleEnvironmentVariablesProvider;
import org.springframework.cloud.task.repository.TaskRepository;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;

import com.example.demo.commons.CommonStepBuilder;
import com.example.demo.commons.CommonsPubSubStep;
import com.example.demo.reader.PubSubTemplateItemReader;

//TODO: Trazer nomes de topicos e outras variaveis a partir do properties.
//	Usar como exemplo o kafka  => https://docs.spring.io/spring-batch/docs/4.3.x/api/org/springframework/batch/item/kafka/KafkaItemReader.html
@Configuration
@EnableBatchProcessing		//para injetar JobBuilderFactory e StepBuilderFactory
public class RemotePartitionJobConfiguration {

	//-- usados no worker INICIO
	@Autowired
	private ConfigurableApplicationContext context;
	
	@Autowired
	public JobRepository jobRepository;
	//-- usados no worker FIM

	
	@Autowired
	private Environment environment;
	
	@Autowired
	private DelegatingResourceLoader resourceLoader;
	
	
    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

	@Autowired 
	public PubSubTemplateItemReader pubsubTemplateItemReader;
	
	@Autowired
	CommonsPubSubStep commonsPubSubStep;		//para testes
	
	
	@Value("${particionamento.image}")
	private String image;
	
	@Bean
	@Profile("!worker")
	public Job remotePartitionedJob() {
		return jobBuilderFactory.get( "remotePartitionedJob")
				// popula pubsub com dados de testes
				.start( commonsPubSubStep.stepPopulatePubSubWithTestData())
				// executa task distribuido kubernates
				.next( partitionedStep( remoteDeployerPartitionHandler( null, null, null)))
				.build();
	}

	@Bean
	@Profile("!worker")
	public PartitionHandler remoteDeployerPartitionHandler(TaskLauncher taskLauncher,
			 JobExplorer jobExplorer,
			 TaskRepository taskRepository) {

		Resource resource = this.resourceLoader.getResource(image);
		DeployerPartitionHandler partitionHandler =  new DeployerPartitionHandler(
				taskLauncher, jobExplorer, resource, 
				"workerStep", //<= OBS: Aqui tem que ser o nome do rotina do step do worker, e não o nome no get()
				taskRepository); 
		List<String> commandLineArgs = new ArrayList<>(3);
		commandLineArgs.add("--spring.profiles.active=worker");
		commandLineArgs.add("--spring.cloud.task.initialize-enabled=false");
		commandLineArgs.add("--spring.batch.initializer.enabled=false");
		partitionHandler.setCommandLineArgsProvider(new PassThroughCommandLineArgsProvider(commandLineArgs));
		partitionHandler.setEnvironmentVariablesProvider(new SimpleEnvironmentVariablesProvider(this.environment));
		partitionHandler.setMaxWorkers(10);
		partitionHandler.setGridSize(2);
		partitionHandler.setApplicationName("PartitionedPubSubJobTask");
		return partitionHandler;

	}

	
	public Step partitionedStep(PartitionHandler remoteDeployerPartitionHandler) {
		
		return stepBuilderFactory.get( "partitionedStep")
				.partitioner( "partitioned-step", partitioner())  //<- particiona no master
				.step( workerStep())	//<- step executado no worker
				.partitionHandler( remoteDeployerPartitionHandler)  //<- cuida dos dois lados..
				.build();
	}
	

	private Partitioner partitioner() {
		return new Partitioner() {

			@Override
			public Map<String, ExecutionContext> partition(int gridSize) {
				Map<String, ExecutionContext> result = new HashMap<>();
				
				for( int i= 1; i<= 2; i++) {
					ExecutionContext value = new ExecutionContext();
					result.put( "partition"+i, value);
					value.putInt( "value", i);
				}
				return result;
			}
		};
	}	
	
	@Bean
	public Step workerStep() {
		// como nome da step é melhor usar o mesmo nome da rotina porque aparentemente 
		// o DeployerPartitionHandler só entende pelo nome da rotina, e não o nome dado aqui
		return this.stepBuilderFactory.get( "workerStep")   //<= usar o nome da rotina!
			.<String,String>chunk(1)
			.reader( pubsubTemplateWithPopulateItemReader(null))
			.writer( CommonStepBuilder.writer())
			.build();
	}
	
	@Bean
	@StepScope
	public ItemReader<String> pubsubTemplateWithPopulateItemReader(
		 @Value("#{stepExecutionContext['value']}") Integer value
			
		) {
		System.out.println( String.format( "partition value= %d", value));
		
		return pubsubTemplateItemReader
				.projectId("playground--sergio")
				.subscriptionId("mysub")
				.build();
	}	

	// Entry point para o worker..
	// veja que não é específico para o step, que vem via parametro...
	@Bean
	@Profile("worker")
	public DeployerStepExecutionHandler stepExecutionHandler(JobExplorer jobExplorer) {
		return new DeployerStepExecutionHandler(this.context, jobExplorer, jobRepository);
	}	
}

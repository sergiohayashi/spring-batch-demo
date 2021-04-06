package com.example.demo.job;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
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

//TODO: As propriedades do yaml do kubernates só vão para o master, e não para os workers. Como contornar?

@Configuration
@EnableBatchProcessing // para injetar JobBuilderFactory e StepBuilderFactory
public class RemotePartitionJobConfiguration {

	// -- usados no worker INICIO
	@Autowired
	private ConfigurableApplicationContext context;

	@Autowired
	public JobRepository jobRepository;
	// -- usados no worker FIM

	@Autowired
	private Environment environment;

	@Autowired
	private DelegatingResourceLoader resourceLoader;

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;

	@Value("${worker.image}")
	private String image;
	
	@Value("${worker.gridsize:1}")
	private Integer gridSize; 

	@Value("${job.name:}")
	private String jobName; 

	@Value("${simulate.error.for.steps:}")
	private String simulateErrorForSteps; 
	
	@Bean
	@Profile("!worker")
	public Job remotePartitionedJob() {
		return jobBuilderFactory.get( 
					jobName==null || jobName.length()<=0? 
					"remotePartitionedJob-"+ new Random().nextInt():jobName)		
				.start( simpleLocalStep( "INICIO", "INICIO"))
				.next( partitionedStep( remoteDeployerPartitionHandler( null, null, null)))
				.next( simpleLocalStep( "FIM", "FIM"))
				.build();
	}
	
	
	public Step simpleLocalStep( String tag, String msg) {
		return stepBuilderFactory.get( "simpleLocalStep-"+ tag)
				.tasklet( CommonStepBuilder.simplePrintTask( msg))
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
		partitionHandler.setMaxWorkers(10);		//<= aparentemente nao tem muito efeito...
		partitionHandler.setGridSize(gridSize);  
		partitionHandler.setApplicationName("SimplePartitionedTask");
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
				System.out.println( "partitioner: jobName= "+ jobName);
				System.out.println( "partitioner: GridSize= "+ gridSize);
				System.out.println( "partitioner: simulateErrorForSteps= "+ simulateErrorForSteps);
				
				Map<String, ExecutionContext> result = new HashMap<>();
				
				for( int i= 1; i<= gridSize; i++) {
					ExecutionContext value = new ExecutionContext();
					result.put( "partition"+i, value);
					value.putInt( "value", i);
					int errFlg= simulateErrorForSteps!= null && 
							Arrays.asList( simulateErrorForSteps.split(",")).contains( ""+i)? 1: 0;
					value.putInt( "errorFlg", errFlg);
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
			.tasklet( simpleLocalTaskAtRemote( null))		// executa uma task, mas poderia chunked step..
			.build();
	}

	/**
	 * Step do worker.
	 * 
	 * As propriedades do yaml do kubernates só vão para o master, e não para os workers. Como contornar?
	 * Portanto, os @Value também são setados somente quando estão no application.properties.
	 * Quando sobrescrito pelo yaml não mudam de valor.
	 * 
	 * Por outro lado, para o worker, os envs no yaml são passados vai variavel de ambiente.
	 * Ex: 
	 *      - name: simulate__error__for__steps
            value: '101' 
	 * 	Pode ser obtido por environment.getProperty( "simulate__error__for__steps")
	 * 
	 * TODO: Deve ter um jeito melhor...
	 * 
	 * 
	 * @param value
	 * @return
	 */
	@Bean
	@StepScope		//<= stepScope não consegue pegar variaveis setadas por @Value..
	public Tasklet simpleLocalTaskAtRemote(
		 @Value("#{stepExecutionContext['value']}") Integer value
		) {
		//-----testes---------------
		System.out.println( "simpleLocalTaskAtRemote: env.simulate__error__for__steps= "+ 
				environment.getProperty( "simulate__error__for__steps"));   // print OK!
		System.out.println( "simpleLocalTaskAtRemote: jobName= "+ jobName);	// print null
		
		//------------------
		String simulateErrorForSteps= environment.getProperty( "simulate__error__for__steps");
		int errFlg= simulateErrorForSteps!= null && 
				Arrays.asList( simulateErrorForSteps.split(",")).contains( value.toString())? 1: 0;
		System.out.println( String.format( "Iniciando partição  %d, err=%d", value, errFlg));
		
		if( errFlg== 1) {
			return CommonStepBuilder.simpleExceptionTask( "Particao "+ value+"  com simulação de erro!");
		} else {
			return CommonStepBuilder.simplePrintTask( String.format( "\nExecutando tarefa da partição %d...\n", value));
		}
	}	 
	
	
	// Entry point para o worker..
	// veja que não é específico para o step, que vem via parametro...
	@Bean
	@Profile("worker")
	public DeployerStepExecutionHandler stepExecutionHandler(JobExplorer jobExplorer) {
		return new DeployerStepExecutionHandler(this.context, jobExplorer, jobRepository);
	}		
	
}

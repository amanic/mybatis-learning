# SpringBoot源码解析

## SpringBoot源码解析-启动流程（一）

**前言：** 读过spring源码的读者应该知道，spring源码有一个特点就是从顶层看，逻辑并不复杂。但是深入了看，spring为了实现一个逻辑会做大量的工作。想要一下子找到spring一个逻辑的源头并不容易。

所以我建议在分析源码的时候会使用层层突进，重点突破的策略。千万别看到一个方法就钻进去，这样很容易迷失在代码中。

### 开始分析代码

```java
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}

    public static ConfigurableApplicationContext run(Class<?> primarySource,
			String... args) {
		return run(new Class<?>[] { primarySource }, args);
	}
	
	public static ConfigurableApplicationContext run(Class<?>[] primarySources,
			String[] args) {
		return new SpringApplication(primarySources).run(args);
	}

```

从springboot入口的run方法点进来，到了这儿。可以看到新建了一个SpringApplication对象，然后调用了该对象得到run方法。 所以我们一步一步来，这一节先分析新建的过程。

### SpringApplication新建过程

```java
public SpringApplication(Class<?>... primarySources) {
	this(null, primarySources);
}

public SpringApplication(ResourceLoader resourceLoader, Class<?>... primarySources) {
	//前三行代码校验，设置参数，没啥好说的
	this.resourceLoader = resourceLoader;
	Assert.notNull(primarySources, "PrimarySources must not be null");
	this.primarySources = new LinkedHashSet<>(Arrays.asList(primarySources));
	
	//判断web容器的类型
	this.webApplicationType = WebApplicationType.deduceFromClasspath();
	
	//初始化ApplicationContextInitializer实例
	setInitializers((Collection) getSpringFactoriesInstances(
			ApplicationContextInitializer.class));
			
	//初始化ApplicationListener实例
	setListeners((Collection) getSpringFactoriesInstances(ApplicationListener.class));
	
	//判断那个是入口类
	this.mainApplicationClass = deduceMainApplicationClass();
}
```

新建代码如上，primarySources参数就是我们一开始传入的`Application.class//启动类`，resourceLoader为null。 前三行主要是校验以及设置参数。第四行，判断了web容器的类型,代码如下:

```java
static WebApplicationType deduceFromClasspath() {
	if (ClassUtils.isPresent(WEBFLUX_INDICATOR_CLASS, null)
			&& !ClassUtils.isPresent(WEBMVC_INDICATOR_CLASS, null)
			&& !ClassUtils.isPresent(JERSEY_INDICATOR_CLASS, null)) {
		return WebApplicationType.REACTIVE;
	}
	for (String className : SERVLET_INDICATOR_CLASSES) {
		if (!ClassUtils.isPresent(className, null)) {
			return WebApplicationType.NONE;
		}
	}
	return WebApplicationType.SERVLET;
}
```

主要看加载的包中，是否存在相应的类。我们主要用的是web模块，这个地方返回的是`WebApplicationType.SERVLET`。

下面开始了初始化`SpringBootApplication`的`private List<ApplicationContextInitializer<?>> initializers`变量，代码如下:

```java
private <T> Collection<T> getSpringFactoriesInstances(Class<T> type) {
	return getSpringFactoriesInstances(type, new Class<?>[] {});
}

private <T> Collection<T> getSpringFactoriesInstances(Class<T> type,
		Class<?>[] parameterTypes, Object... args) {
	ClassLoader classLoader = getClassLoader();
	
	//关键地方，类名就是从这儿加载的
	Set<String> names = new LinkedHashSet<>(
			SpringFactoriesLoader.loadFactoryNames(type, classLoader));
			
	//实例化刚刚获取的类
	List<T> instances = createSpringFactoriesInstances(type, parameterTypes,
			classLoader, args, names);
	//排序
	AnnotationAwareOrderComparator.sort(instances);
	return instances;
}
```

点开loadFactoryNames方法，继续打开其中的loadSpringFactories方法：

```java
private static Map<String, List<String>> loadSpringFactories(@Nullable ClassLoader classLoader) {
        ...
		try {
			Enumeration<URL> urls = (classLoader != null ?
					classLoader.getResources(FACTORIES_RESOURCE_LOCATION) :
					ClassLoader.getSystemResources(FACTORIES_RESOURCE_LOCATION));
			result = new LinkedMultiValueMap<>();
			while (urls.hasMoreElements()) {
				URL url = urls.nextElement();
				UrlResource resource = new UrlResource(url);
				Properties properties = PropertiesLoaderUtils.loadProperties(resource);
				for (Map.Entry<?, ?> entry : properties.entrySet()) {
					String factoryClassName = ((String) entry.getKey()).trim();
					for (String factoryName : StringUtils.commaDelimitedListToStringArray((String) entry.getValue())) {
						result.add(factoryClassName, factoryName.trim());
					}
				}
			}
			cache.put(classLoader, result);
			return result;
		}
       ...
	}
```

可以看到在`loadSpringFactories`方法中加载了`META-INF/spring.factories`文件的信息，并将其存入了map里。然后在`loadFactoryNames`中依据类名获取了相应的集合。那么根据刚刚传进来的类名`ApplicationContextInitializer`，我们去瞧一瞧配置文件里配了啥。打开springboot的jar包，找到`spring.factories`文件，发现里面配置如下。

```xml
# Application Context Initializers
org.springframework.context.ApplicationContextInitializer=\
org.springframework.boot.context.ConfigurationWarningsApplicationContextInitializer,\
org.springframework.boot.context.ContextIdApplicationContextInitializer,\
org.springframework.boot.context.config.DelegatingApplicationContextInitializer,\
org.springframework.boot.web.context.ServerPortInfoApplicationContextInitializer

# Initializers
org.springframework.context.ApplicationContextInitializer=\
org.springframework.boot.autoconfigure.SharedMetadataReaderFactoryContextInitializer,\
org.springframework.boot.autoconfigure.logging.ConditionEvaluationReportLoggingListener
```

到这儿我们就发现了，原来setInitializers方法，就是找的这几个类。

继续往下看在new一个SpringBootApplication的时候初始化`ApplicationListener`实例的方法，这个方法的逻辑和上面初始化`ApplicationContextInitializer`一模一样，所以就不详解了。只需要知道springboot到那个地方，找到了那几个类就可以。几个ApplicationListener类名如下:

```xml
# Application Listeners
org.springframework.context.ApplicationListener=\
org.springframework.boot.ClearCachesApplicationListener,\
org.springframework.boot.builder.ParentContextCloserApplicationListener,\
org.springframework.boot.context.FileEncodingApplicationListener,\
org.springframework.boot.context.config.AnsiOutputApplicationListener,\
org.springframework.boot.context.config.ConfigFileApplicationListener,\
org.springframework.boot.context.config.DelegatingApplicationListener,\
org.springframework.boot.context.logging.ClasspathLoggingApplicationListener,\
org.springframework.boot.context.logging.LoggingApplicationListener,\
org.springframework.boot.liquibase.LiquibaseServiceLocatorApplicationListener
```

最后还有一行代码：`this.mainApplicationClass = deduceMainApplicationClass();`

这行代码逻辑就是获取方法的调用栈，然后找到main方法的那个类。显然，这边的mainApplicationClass就算我们入口的那个Application类。

到这儿，SpringApplication新建过程就结束了。总结一下，在SpringApplication新建过程中，程序主要还是以设置属性为主。这些属性现在我们只需要知道他们是啥就行，后面再针对功能进行分析。

## SpringBoot源码解析-启动流程（二）

上一节，我们分析了springboot启动流程中SpringApplication 类的新建过程。知道了其在新建过程中导入了几个Initializer和Listener，这一节分析一下run方法的执行逻辑。

```java
public ConfigurableApplicationContext run(String... args) {
   //stopwatch主要是一个监控工具，记录启动的耗时。
   StopWatch stopWatch = new StopWatch();
   stopWatch.start();
   ConfigurableApplicationContext context = null;
   Collection<SpringBootExceptionReporter> exceptionReporters = new ArrayList<>();
   //配置java的headless模式
   configureHeadlessProperty();
   //获取linstener并启动他们
   SpringApplicationRunListeners listeners = getRunListeners(args);
   listeners.starting();
   try {
      //记录启动时输入的参数
      ApplicationArguments applicationArguments = new DefaultApplicationArguments(
            args);
      //准备环境相关配置
      ConfigurableEnvironment environment = prepareEnvironment(listeners,
            applicationArguments);
      configureIgnoreBeanInfo(environment);
      //打印启动标志
      Banner printedBanner = printBanner(environment);
      //创建applicationContext
      context = createApplicationContext();
      exceptionReporters = getSpringFactoriesInstances(
            SpringBootExceptionReporter.class,
            new Class[] { ConfigurableApplicationContext.class }, context);
      //准备并且刷新applicationContext
      prepareContext(context, environment, listeners, applicationArguments,
            printedBanner);
      refreshContext(context);
      afterRefresh(context, applicationArguments);
      stopWatch.stop();
      if (this.logStartupInfo) {
         new StartupInfoLogger(this.mainApplicationClass)
               .logStarted(getApplicationLog(), stopWatch);
      }
      listeners.started(context);
      callRunners(context, applicationArguments);
   }
   catch (Throwable ex) {
      handleRunFailure(context, ex, exceptionReporters, listeners);
      throw new IllegalStateException(ex);
   }

   try {
      listeners.running(context);
   }
   catch (Throwable ex) {
      handleRunFailure(context, ex, exceptionReporters, null);
      throw new IllegalStateException(ex);
   }
   return context;
}
```

配合注释，开始分析run方法的逻辑。

StopWatch主要起一个监控的作用，里面的逻辑并不复杂。后面配置了java的headless模式，这个模式和我们的主流程逻辑影响不大，读者可以自行了解一下。

**在下一行，就到了获取listeners并调用其start方法。这个地方一般人肯定会以为springboot会直接获取在新建时已经得到的listener类。但是 springboot并没有这样做**，这个地方就体现了我一开始说的，有时候我们感觉很简单的逻辑，springboot想的比我们更多，所以做的也更复杂。所以关于listener类的调用我们先不深入分析，后面结合配置的加载一并分析，在这儿，我们只需要知道调用了listener类的start方法即可。

在后面一行，使用ApplicationArguments 记录了启动时的参数，并且使用参数配置了环境类environment。environment主要的作用就是记录了我们在配置文件中配置的那些键值对。**所以经过这步之后我们的配置就已经被读取到项目中了。**

printBanner方法主要的作用就是打印了启动时的标志。

再往下，则开始了对applicationContext类的创建及准备操作。如果读过spring源码的话应该知道对spring框架来说这是一个极其重要的类。这个类启动完成，就代表spring容器的创建完成了。在springboot中applicationContext还负责了内嵌服务器（比如tomcat之类）的启动工作。所以这部分逻辑，等到后面类加载过程以及内嵌tomcat启动分析的时候，在详细深入分析。

## SpringBoot源码解析-配置文件的加载

一般框架，启动之后都会尽快加载配置文件，springboot也不例外，下面就开始分析一下springboot加载配置文件的流程。

springboot配置的加载是从listener类开始的，还记得上一节我说listener类的调用没那么简单么，这一节就先从listener类的调用开始。

run方法中，listeners初始化的地方：

```java
public ConfigurableApplicationContext run(String... args) {
    ...
	SpringApplicationRunListeners listeners = getRunListeners(args);
	listeners.starting();
    ...
}
```

listener类在SpringApplication对象初始化的时候，我们已经从配置文件中获取到了，并存放在了集合里，那么这边为什么没有直接调用而是又绕了一个逻辑呢，先进入getRunListeners方法。

```java
private SpringApplicationRunListeners getRunListeners(String[] args) {
	Class<?>[] types = new Class<?>[] { SpringApplication.class, String[].class };
	return new SpringApplicationRunListeners(logger, getSpringFactoriesInstances(
			SpringApplicationRunListener.class, types, this, args));
}
```

`getRunListeners`方法中，首先获取了`SpringApplicationRunListener`对象，并且使用`SpringApplication`，和args作为的构造函数的参数。然后在使用获得的`SpringApplicationRunListener`对象集合作为参数，构造了`SpringApplicationRunListeners`对象。 我们先去看看`SpringApplicationRunListener`对象是啥。

**`getSpringFactoriesInstances`这个方法大家 应该很熟了，从SpringApplication对象新建时候就一直在调用(设置SpringApplication的Initializers和Listeners的时候)**，所以我们可以直接到配置文件中看一下，获取的`SpringApplicationRunListener`对象到底是啥。

```xml
# Run Listeners
org.springframework.boot.SpringApplicationRunListener=\
org.springframework.boot.context.event.EventPublishingRunListener
```

即`EventPublishingRunListener`:

```java
public class EventPublishingRunListener implements SpringApplicationRunListener, Ordered {
	public EventPublishingRunListener(SpringApplication application, String[] args) {
		this.application = application;
		this.args = args;
		this.initialMulticaster = new SimpleApplicationEventMulticaster();
		for (ApplicationListener<?> listener : application.getListeners()) {
			this.initialMulticaster.addApplicationListener(listener);
		}
	}
}
```

在配置文件中发现了`EventPublishingRunListener`对象，这就是`getRunListeners`方法中获得到的`SpringApplicationRunListener`对象，构造函数很简单，我就不详细分析了。**原本`SpringApplication`类中的listener对象，现在被封装到了`EventPublishingRunListener`对象中。**

回过头来再看，`SpringApplicationRunListener`类又被封装到了`SpringApplicationRunListeners`对象中，这样`getRunListeners`方法的逻辑就执行完了。

现在看看`listeners.starting()`方法的调用逻辑。

```java
public void starting() {
	for (SpringApplicationRunListener listener : this.listeners) {
		//遍历调用starting方法
		listener.starting();
	}
}

public void starting() {
    //这个地方封装了一个事件，大概猜一下应该是打算使用策略模式
	this.initialMulticaster.multicastEvent(
			new ApplicationStartingEvent(this.application, this.args));
}

public void multicastEvent(ApplicationEvent event) {
	multicastEvent(event, resolveDefaultEventType(event));
}

public void multicastEvent(final ApplicationEvent event, @Nullable ResolvableType eventType) {
	ResolvableType type = (eventType != null ? eventType : resolveDefaultEventType(event));
	//getApplicationListeners获取了符合策略的监听器
	for (final ApplicationListener<?> listener : getApplicationListeners(event, type)) {
		Executor executor = getTaskExecutor();
		if (executor != null) {
			executor.execute(() -> invokeListener(listener, event));
		}
		else {
			invokeListener(listener, event);
		}
	}
}

protected void invokeListener(ApplicationListener<?> listener, ApplicationEvent event) {
            ...
		doInvokeListener(listener, event);
	...
}

private void doInvokeListener(ApplicationListener listener, ApplicationEvent event) {
           ...
		listener.onApplicationEvent(event);
           ...
}
```

最后终于在doInvokeListener方法中，看到了监听器的执行。所有监听器的执行都使用的策略模式，如果想符合某些事件，在监听器的onApplicationEvent方法中配置一下即可。在这儿，我们也可以感受到spring框架设计的规范性，使用策略模式可以很方便的基于事件做相应扩展。

------

上面我们已经了解了listener类的启动逻辑，下面开始正式分析配置文件的加载。

```java
public ConfigurableApplicationContext run(String... args) {
         ...
		ApplicationArguments applicationArguments = new DefaultApplicationArguments(
				args);
		ConfigurableEnvironment environment = prepareEnvironment(listeners,
				applicationArguments);
      ...
}

private ConfigurableEnvironment prepareEnvironment(
		SpringApplicationRunListeners listeners,
		ApplicationArguments applicationArguments) {
	...
	listeners.environmentPrepared(environment);
	...
}
```

在run方法中，找到prepareEnvironment方法，进入之后，会看到监听器启动了environmentPrepared事件，所以我们就去监听器里面，找找看符合环境事件的监听器。

看名字也能看出来，就是他ConfigFileApplicationListener。找到他的onApplicationEvent方法，开始分析。

```java
public void onApplicationEvent(ApplicationEvent event) {
	if (event instanceof ApplicationEnvironmentPreparedEvent) {
	    //入口在这儿
		onApplicationEnvironmentPreparedEvent(
				(ApplicationEnvironmentPreparedEvent) event);
	}
	if (event instanceof ApplicationPreparedEvent) {
		onApplicationPreparedEvent(event);
	}
}

private void onApplicationEnvironmentPreparedEvent(
		ApplicationEnvironmentPreparedEvent event) {
	List<EnvironmentPostProcessor> postProcessors = loadPostProcessors();
	postProcessors.add(this);
	AnnotationAwareOrderComparator.sort(postProcessors);
	for (EnvironmentPostProcessor postProcessor : postProcessors) {
	    //虽然这边还有其他几个监听器，但是最重要的依然是他本身所以，我们还是分析他本身的postProcessEnvironment方法
		postProcessor.postProcessEnvironment(event.getEnvironment(),
				event.getSpringApplication());
	}
}
```

postProcessEnvironment方法逻辑如下

```java
public void postProcessEnvironment(ConfigurableEnvironment environment,
		SpringApplication application) {
	addPropertySources(environment, application.getResourceLoader());
}

protected void addPropertySources(ConfigurableEnvironment environment,
		ResourceLoader resourceLoader) {
	RandomValuePropertySource.addToEnvironment(environment);
	//关键代码在这儿
	new Loader(environment, resourceLoader).load();
}

            //先看构造函数
	Loader(ConfigurableEnvironment environment, ResourceLoader resourceLoader) {
		this.environment = environment;
		this.placeholdersResolver = new PropertySourcesPlaceholdersResolver(
				this.environment);
		this.resourceLoader = (resourceLoader != null) ? resourceLoader
				: new DefaultResourceLoader();
		//这个方法又见到了，话不多说，打开配置文件
		this.propertySourceLoaders = SpringFactoriesLoader.loadFactories(
				PropertySourceLoader.class, getClass().getClassLoader());
	}

//这就是yml和properties配置支持的来源
org.springframework.boot.env.PropertySourceLoader=\
org.springframework.boot.env.PropertiesPropertySourceLoader,\
org.springframework.boot.env.YamlPropertySourceLoader
```

Loader类在构造函数中获取了yml和properties配置文件的支持。下面开始分析load函数。

```java
	public void load() {
		this.profiles = new LinkedList<>();
			this.processedProfiles = new LinkedList<>();
			this.activatedProfiles = false;
			this.loaded = new LinkedHashMap<>();
			initializeProfiles();
			while (!this.profiles.isEmpty()) {
				Profile profile = this.profiles.poll();
				if (profile != null && !profile.isDefaultProfile()) {
					addProfileToEnvironment(profile.getName());
				}
				load(profile, this::getPositiveProfileFilter, addToLoaded(MutablePropertySources::addLast, false));
				this.processedProfiles.add(profile);
			}
			resetEnvironmentProfiles(this.processedProfiles);
			load(null, this::getNegativeProfileFilter, addToLoaded(MutablePropertySources::addFirst, true));
			addLoadedPropertySources();
	}
>
	private void load(Profile profile, DocumentFilterFactory filterFactory,
			DocumentConsumer consumer) {
		//关注getSearchLocations方法
		getSearchLocations().forEach((location) -> {
			boolean isFolder = location.endsWith("/");
			//关注getSearchNames方法
			Set<String> names = isFolder ? getSearchNames() : NO_SEARCH_NAMES;
			names.forEach(
					(name) -> load(location, name, profile, filterFactory, consumer));
		});
	}
```

在getSearchLocations方法中，可以看到如果没有指定地址的话，默认地址就是"classpath:/,classpath:/config/,file:./,file:./config/"，如果想指定的话，启动时需要加上`spring.config.location`参数：

```java
private Set<String> getSearchLocations() {
  // CONFIG_LOCATION_PROPERTY = spring.config.location
   if (this.environment.containsProperty(CONFIG_LOCATION_PROPERTY)) {
      return getSearchLocations(CONFIG_LOCATION_PROPERTY);
   }
   Set<String> locations = getSearchLocations(CONFIG_ADDITIONAL_LOCATION_PROPERTY);
   locations.addAll(
         asResolvedSet(ConfigFileApplicationListener.this.searchLocations, DEFAULT_SEARCH_LOCATIONS));
   return locations;
}
```

在getSearchNames方法中，可以看到如果没有指定配置文件名称的话，配置文件的名字就按照application来搜索。如果想指定的话，启动时需要加上`spring.config.name`参数:

```java
private Set<String> getSearchNames() {
  // CONFIG_NAME_PROPERTY = spring.config.name
   if (this.environment.containsProperty(CONFIG_NAME_PROPERTY)) {
      String property = this.environment.getProperty(CONFIG_NAME_PROPERTY);
      return asResolvedSet(property, null);
   }
   return asResolvedSet(ConfigFileApplicationListener.this.names, DEFAULT_NAMES);
}
```

所以继续往下看load方法。

```java
	private void load(String location, String name, Profile profile,
			DocumentFilterFactory filterFactory, DocumentConsumer consumer) {
		...
		Set<String> processed = new HashSet<>();
		for (PropertySourceLoader loader : this.propertySourceLoaders) {
			for (String fileExtension : loader.getFileExtensions()) {
				if (processed.add(fileExtension)) {
					loadForFileExtension(loader, location + name, "." + fileExtension,
							profile, filterFactory, consumer);
				}
			}
		}
	}
```

在这一层的load方法中，看到了Loader类新建时，获取的yml和properties格式支持类propertySourceLoaders,查看两个类的getFileExtensions方法。

```java
public class YamlPropertySourceLoader implements PropertySourceLoader {

	@Override
	public String[] getFileExtensions() {
		return new String[] { "yml", "yaml" };
	}
}

public class PropertiesPropertySourceLoader implements PropertySourceLoader {

	private static final String XML_FILE_EXTENSION = ".xml";

	@Override
	public String[] getFileExtensions() {
		return new String[] { "properties", "xml" };
	}
}
```

到了这一步，我们终于摸清了为什么默认的配置文件名字必须是application，而且可以为yml和properties格式。

最后加载的过程其实没啥好分析的了。经过了我们的一通操作，我们已经顺利的摸清了springboot默认配置加载的来源，并且了解了如果想指定配置该怎么做。

## SpringBoot源码解析-Bean的加载与自动化配置

```java
public ConfigurableApplicationContext run(String... args) {
		        ...
		        //创建ApplicationContext
			context = createApplicationContext();
			...
			//做一些初始化配置
			prepareContext(context, environment, listeners, applicationArguments,
					printedBanner);
			refreshContext(context);
			afterRefresh(context, applicationArguments);
			...
	}
```

首先我们进入SpringApplication的run方法中，在run方法中我们看到和ApplicationContext有关的代码一共有4行，第一行创建了ApplicationContext，第二行做了一些初始化配置，第三行调用了refresh方法，读过spring源码的话应该知道这个方法包含了ApplicationContext初始化最重要也最大部分的逻辑，所以这行待会会重点分析，最后一行是一个空方法，留着子类覆写。

### createApplicationContext

```java
protected ConfigurableApplicationContext createApplicationContext() {
	Class<?> contextClass = this.applicationContextClass;
	if (contextClass == null) {
		try {
			switch (this.webApplicationType) {
			case SERVLET:
				contextClass = Class.forName(DEFAULT_SERVLET_WEB_CONTEXT_CLASS);
				break;
			...
			}
		}
	}
	return (ConfigurableApplicationContext) BeanUtils.instantiateClass(contextClass);
}
```

首先进入create方法，在SpringApplication初始化的时候，我们已经知道了这是一个网络服务，所以这边创建的类是DEFAULT_SERVLET_WEB_CONTEXT_CLASS类，`org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext` 在这边直接调用了无参构造函数。先进入构造函数看一下做了那些事情。

```java
/**
 * Create a new {@link AnnotationConfigServletWebServerApplicationContext} that needs
 * to be populated through {@link #register} calls and then manually
 * {@linkplain #refresh refreshed}.
 */
public AnnotationConfigServletWebServerApplicationContext() {
   this.reader = new AnnotatedBeanDefinitionReader(this);
   this.scanner = new ClassPathBeanDefinitionScanner(this);
}
```

初始化了reader和scanner组件，reader是用来注册bean的，scanner是用来扫描bean的。这两个组件初始化的逻辑都不复杂，读者可以自行理解。**因为构造函数会先执行父类的构造函数，所以在这里会初始化一个beanFactory，而这个beanFactory的类型为DefaultListableBeanFactory。**但是重点关注一个地方。在reader的构造函数中:

```java
public AnnotatedBeanDefinitionReader(BeanDefinitionRegistry registry) {
	this(registry, getOrCreateEnvironment(registry));
}

    public AnnotatedBeanDefinitionReader(BeanDefinitionRegistry registry, Environment environment) {
	...
	AnnotationConfigUtils.registerAnnotationConfigProcessors(this.registry);
}

public static void registerAnnotationConfigProcessors(BeanDefinitionRegistry registry) {
	registerAnnotationConfigProcessors(registry, null);
}

public static Set<BeanDefinitionHolder> registerAnnotationConfigProcessors(
		BeanDefinitionRegistry registry, @Nullable Object source) {

	...
	if (!registry.containsBeanDefinition(CONFIGURATION_ANNOTATION_PROCESSOR_BEAN_NAME)) {
		RootBeanDefinition def = new RootBeanDefinition(ConfigurationClassPostProcessor.class);
		def.setSource(source);
		beanDefs.add(registerPostProcessor(registry, def, CONFIGURATION_ANNOTATION_PROCESSOR_BEAN_NAME));
	}

	...
}
```

一个ConfigurationClassPostProcessor的bean被注入到了容器中，这个地方留意一下，后面这个bean很重要。

创建完成了之后，我们看一下prepareContext方法。

### prepareContext

```java
private void prepareContext(ConfigurableApplicationContext context, ConfigurableEnvironment environment,
			SpringApplicationRunListeners listeners, ApplicationArguments applicationArguments, Banner printedBanner) {
		context.setEnvironment(environment);
		postProcessApplicationContext(context);
		applyInitializers(context);
		listeners.contextPrepared(context);
		if (this.logStartupInfo) {
			logStartupInfo(context.getParent() == null);
			logStartupProfileInfo(context);
		}
		// Add boot specific singleton beans
		ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
		beanFactory.registerSingleton("springApplicationArguments", applicationArguments);
		if (printedBanner != null) {
			beanFactory.registerSingleton("springBootBanner", printedBanner);
		}
		if (beanFactory instanceof DefaultListableBeanFactory) {
			((DefaultListableBeanFactory) beanFactory)
					.setAllowBeanDefinitionOverriding(this.allowBeanDefinitionOverriding);
		}
		// Load the sources
		Set<Object> sources = getAllSources();
		Assert.notEmpty(sources, "Sources must not be empty");
		load(context, sources.toArray(new Object[0]));
		listeners.contextLoaded(context);
	}
```

prepareContext方法中，调用了一些监听器，和初始化接口，但是最重要的是load这个方法。load这个方法，将我们main方法的这个类传入了容器中。这个类上面有一个非常重要的注解SpringBootApplication。

### refreshContext

```java
public void refresh() throws BeansException, IllegalStateException {
	synchronized (this.startupShutdownMonitor) {
		// Prepare this context for refreshing.
		// 准备更新上下文，设置开始时间，标记活动标志，初始化配置文件中的占位符
    //initPropertySources 增加一些property的提前引入
		//validateRequiredProperties 验证环境中是否存在setRequiredProperties设置的property
		prepareRefresh();

		// Tell the subclass to refresh the internal bean factory.
    	/**
	 * 这里需要分两种情况讨论：
	 * 一、 web工程(非SpringBoot) AbstractRefreshableApplicationContext,将bean定义加载到给定的        *BeanFactory中
	 * 		1. createBeanFactory(); 为此上下文创建内部 BeanFactory
	 * 		2. customizeBeanFactory(beanFactory); 定制 BeanFactory，是否允许 BeanDefinition 覆盖、是    *    否允许循环引用
	 * 		3. loadBeanDefinitions(beanFactory); 通过 BeanDefinitionReader 解析 xml 文件，解析封装信    *    息到 BeanDefinition，并将其 register 到 BeanFactory 中，以 beanName为key将beanDefinition    *    存到 DefaultListableBeanFactory#beanDefinitionMap 中
	 * 二、 SpringBoot GenericApplicationContext，实际 register 过程在                              * invokeBeanFactoryPostProcessors 中
	 */
		ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();

		// Prepare the bean factory for use in this context.
		// 准备 BeanFactory 以便在此上下文中使用。
		// 1. 设置 BeanFactory 的类加载器
		// 2. 添加几个 BeanPostProcessor，
		// 3. 实例化几个特殊的 bean
		prepareBeanFactory(beanFactory);

		try {
			// Allows post-processing of the bean factory in context subclasses.
			// 在 AbstractApplicationContext#postProcessBeanFactory 为空实现，留给子类做扩展，不同 ApplicationContext 实现不同，不作详细描述
			postProcessBeanFactory(beanFactory);

			// Invoke factory processors registered as beans in the context.
			// Spring 的 SPI
			// 先调用 BeanDefinitionRegistryPostProcessor 和 ImportBeanDefinitionRegistrar 的实现类
			// 再调用 BeanFactoryPostProcessor 各个实现类的 postProcessBeanFactory(factory) 方法
			// 例如：ConfigurationClassPostProcessor 会扫描 <context:component-scan/> 和 @SpringBootApplication(scanBasePackages = "") 中的Component，并且将 @Configuration 类中的 @Bean register 到 BeanFactory 中
			// 扩展例如：MyBatis MapperScannerConfigurer 和 MapperScannerRegistrar，扫描Mapper register 到 BeanFactory 中
			invokeBeanFactoryPostProcessors(beanFactory);

			// Register bean processors that intercept bean creation.
			// 注册 BeanPostProcessor 的实现类，不同于刚刚的 BeanFactoryPostProcessor
			// BeanPostProcessor 接口两个方法 postProcessBeforeInitialization 和 postProcessAfterInitialization 会在 Bean 初始化之前和之后调用
			// 这边 Bean 还没初始化，下面的 finishBeanFactoryInitialization 才是真正的初始化方法
			registerBeanPostProcessors(beanFactory);

			// Initialize message source for this context.
			// 初始化当前 ApplicationContext 的 MessageSource，解析消息的策略接口，用于支持消息的国际化和参数化
			// Spring 两个开箱即用的实现 ResourceBundleMessageSource 和 ReloadableResourceBundleMessageSource
			initMessageSource();

			// Initialize event multicaster for this context.
			// 初始化当前 ApplicationContext 的事件广播器
			initApplicationEventMulticaster();

			// Initialize other special beans in specific context subclasses.
			// 典型模板方法
			// 子类可以在实例化 bean 之前，做一些初始化工作，SpringBoot 会在这边启动 Web 服务
			onRefresh();

			// Check for listener beans and register them.
			// 向 initApplicationEventMulticaster() 初始化的 applicationEventMulticaster 注册事件监听器，就是实现 ApplicationListener 接口类
			// 观察者模式，例如实现了 ApplicationEvent，通过 ApplicationEventPublisher#publishEvent()，可以通知到各个 ApplicationListener#onApplicationEvent
			registerListeners();

			// Instantiate all remaining (non-lazy-init) singletons.
			// 初始化所有的 singletons bean（lazy-init 的除外）
			// Spring bean 初始化核心方法
			finishBeanFactoryInitialization(beanFactory);

			// Last step: publish corresponding event.
			// ApplicationEventPublisher#publishEvent() 初始化完成（ContextRefreshedEvent）事件
			finishRefresh();
		}

		catch (BeansException ex) {
			if (logger.isWarnEnabled()) {
				logger.warn("Exception encountered during context initialization - " +
						"cancelling refresh attempt: " + ex);
			}

			// Destroy already created singletons to avoid dangling resources.
			// destroy 已经创建的 singleton 避免占用资源
			destroyBeans();

			// Reset 'active' flag.
			// 重置'有效'标志
			cancelRefresh(ex);

			// Propagate exception to caller.
			throw ex;
		}

		finally {
			// Reset common introspection caches in Spring's core, since we
			// might not ever need metadata for singleton beans anymore...
			// 重置Spring核心中的常见内省缓存，因为可能不再需要单例bean的元数据了...
			resetCommonCaches();
		}
	}
}
```

挑重点讲：

#### invokeBeanFactoryPostProcessors

```java
	protected void invokeBeanFactoryPostProcessors(ConfigurableListableBeanFactory beanFactory) {
		PostProcessorRegistrationDelegate.invokeBeanFactoryPostProcessors(beanFactory, getBeanFactoryPostProcessors());
		...
	}
	
public static void invokeBeanFactoryPostProcessors(
			ConfigurableListableBeanFactory beanFactory, List<BeanFactoryPostProcessor> beanFactoryPostProcessors) {

		    ...
		    List<BeanDefinitionRegistryPostProcessor> currentRegistryProcessors = new ArrayList<>();
			String[] postProcessorNames =
					beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
			for (String ppName : postProcessorNames) {
				if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
					currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
					processedBeans.add(ppName);
				}
			}
			sortPostProcessors(currentRegistryProcessors, beanFactory);
			registryProcessors.addAll(currentRegistryProcessors);
			invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
			currentRegistryProcessors.clear();
			...
	}
```

在`invokeBeanFactoryPostProcessors`方法中，从容器中获取了`BeanDefinitionRegistryPostProcessor`类型的类，然后执行了这些类的`postProcessBeanDefinitionRegistry`方法。还记得上面我让你们重点关注的`ConfigurationClassPostProcessor`么，他就是实现了`BeanDefinitionRegistryPostProcessor`，所以这个地方会调用`ConfigurationClassPostProcessor`的`postProcessBeanDefinitionRegistry`方法。那么我们进入方法瞧瞧。

```java
public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
	...
	processConfigBeanDefinitions(registry);
}

public void processConfigBeanDefinitions(BeanDefinitionRegistry registry) {
	List<BeanDefinitionHolder> configCandidates = new ArrayList<>();
	String[] candidateNames = registry.getBeanDefinitionNames();

	for (String beanName : candidateNames) {
		BeanDefinition beanDef = registry.getBeanDefinition(beanName);
		...
		//判断@Configuration注解
		else if (ConfigurationClassUtils.checkConfigurationClassCandidate(beanDef, this.metadataReaderFactory)) {
			configCandidates.add(new BeanDefinitionHolder(beanDef, beanName));
		}
	}
	...
	ConfigurationClassParser parser = new ConfigurationClassParser(
			this.metadataReaderFactory, this.problemReporter, this.environment,
			this.resourceLoader, this.componentScanBeanNameGenerator, registry);

	Set<BeanDefinitionHolder> candidates = new LinkedHashSet<>(configCandidates);
	Set<ConfigurationClass> alreadyParsed = new HashSet<>(configCandidates.size());
	do {
	//解析带有@Configuration注解的类
		parser.parse(candidates);
	...
}
```

`processConfigBeanDefinitions`方法主要有两个逻辑，首先判断类上是否带有`@Configuration`注解，然后解析该类。其实在这儿，主要解析的就是`@SpringBootApplication`注解。因为其包含了`@Configuration`注解，在前面已经分析过了。进入parse方法。

```java
public void parse(Set<BeanDefinitionHolder> configCandidates) {
	for (BeanDefinitionHolder holder : configCandidates) {
		BeanDefinition bd = holder.getBeanDefinition();
		try {
			if (bd instanceof AnnotatedBeanDefinition) {
				parse(((AnnotatedBeanDefinition) bd).getMetadata(), holder.getBeanName());
			}
	...

	this.deferredImportSelectorHandler.process();
}
```

主要有两个逻辑，我们一个一个来分析。首先再次进入parse方法:

```java
protected final void parse(AnnotationMetadata metadata, String beanName) throws IOException {
	processConfigurationClass(new ConfigurationClass(metadata, beanName));
}

protected void processConfigurationClass(ConfigurationClass configClass) throws IOException {
	...
	SourceClass sourceClass = asSourceClass(configClass);
	do {
	//进入这个方法
		sourceClass = doProcessConfigurationClass(configClass, sourceClass);
	}
	while (sourceClass != null);

	this.configurationClasses.put(configClass, configClass);
}
```

在doProcessConfigurationClass中，我们看到了熟悉的`Component，PropertySources，ComponentScan，ImportResource，以及Import注解`，上述几个注解的功能大家应该都很熟悉了，在之前分析`@Import注解的解析`的章节已经讲过了，这些注解在这儿就完成了他们的使命，经过这个方法后，我们自己写的类就会全部进入`springboot`容器中了。

```java
public void process() {
			List<DeferredImportSelectorHolder> deferredImports = this.deferredImportSelectors;
			this.deferredImportSelectors = null;
			try {
				if (deferredImports != null) {
				...
		}
}
```

进入方法后发现如果`deferredImportSelectors`为空的话，就什么都做不了。但是调用debug后发现这个地方是有值的，那么他是什么时候被放进来的呢。我们回头看刚刚的`doProcessConfigurationClass`方法。

```java
protected final SourceClass doProcessConfigurationClass(ConfigurationClass configClass, SourceClass sourceClass)
	...
	// Process any @Import annotations
	processImports(configClass, sourceClass, getImports(sourceClass), true);
	...
}

private void processImports(ConfigurationClass configClass, SourceClass currentSourceClass,
		Collection<SourceClass> importCandidates, boolean checkForCircularImports) {

	...
					if (selector instanceof DeferredImportSelector) {
						this.deferredImportSelectorHandler.handle(
								configClass, (DeferredImportSelector) selector);
					...
	}
}

	public void handle(ConfigurationClass configClass, DeferredImportSelector importSelector) {
		...
			this.deferredImportSelectors.add(holder);
		}
	}
```

在processImports发现了添加的痕迹。但是添加有个前提条件是要import导入的类`selector instanceof DeferredImportSelector`，这个条件是怎么实现的呢？答案也在`@SpringBootApplication`注解中，这个注解包括了`@EnableAutoConfiguration`注解，而该注解源码如下：

```java
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(excludeFilters = {
		@Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class),
		@Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class) })
public @interface SpringBootApplication {

@AutoConfigurationPackage
@Import(AutoConfigurationImportSelector.class)
public @interface EnableAutoConfiguration {

public class AutoConfigurationImportSelector
		implements DeferredImportSelector, BeanClassLoaderAware, ResourceLoaderAware,
		BeanFactoryAware, EnvironmentAware, Ordered {
}
```

所以到这儿我们就知道了`deferredImportSelectors`里面有一个元素，就是这边的`AutoConfigurationImportSelector`。

所以到这儿，我们就可以接着分析process方法了.

```java
	public void process() {
		List<DeferredImportSelectorHolder> deferredImports = this.deferredImportSelectors;
		this.deferredImportSelectors = null;
		try {
			if (deferredImports != null) {
				DeferredImportSelectorGroupingHandler handler = new DeferredImportSelectorGroupingHandler();
				deferredImports.sort(DEFERRED_IMPORT_COMPARATOR);
				//注册
				deferredImports.forEach(handler::register);
				//解析
				handler.processGroupImports();
			}
		}
		finally {
			this.deferredImportSelectors = new ArrayList<>();
		}
	}
```

一个注册方法，一个解析方法，注册方法逻辑比较简单，我们直接进入解析方法。

```java
	public void processGroupImports() {
		for (DeferredImportSelectorGrouping grouping : this.groupings.values()) {
		//这个地方看一下getImports方法
			grouping.getImports().forEach(entry -> {
				...
				//这个方法标记一下，processImport待会回来
					processImports(configurationClass, asSourceClass(configurationClass),
							asSourceClasses(entry.getImportClassName()), false);
				...
		}
	}

	public Iterable<Group.Entry> getImports() {
		for (DeferredImportSelectorHolder deferredImport : this.deferredImports) {
		//重点看process方法
			this.group.process(deferredImport.getConfigurationClass().getMetadata(),
					deferredImport.getImportSelector());
		}
		return this.group.selectImports();
	}

	public void process(AnnotationMetadata annotationMetadata,
			DeferredImportSelector deferredImportSelector) {
		...
		AutoConfigurationEntry autoConfigurationEntry = ((AutoConfigurationImportSelector) deferredImportSelector)
				.getAutoConfigurationEntry(getAutoConfigurationMetadata(),
						annotationMetadata);
		...
	}

protected AutoConfigurationEntry getAutoConfigurationEntry(
		AutoConfigurationMetadata autoConfigurationMetadata,
		AnnotationMetadata annotationMetadata) {
	...
	List<String> configurations = getCandidateConfigurations(annotationMetadata,
			attributes);
	...
}

protected List<String> getCandidateConfigurations(AnnotationMetadata metadata,
		AnnotationAttributes attributes) {
	List<String> configurations = SpringFactoriesLoader.loadFactoryNames(
			getSpringFactoriesLoaderFactoryClass(), getBeanClassLoader());
	...
	return configurations;
}
```

`SpringFactoriesLoader.loadFactoryNames`这个方法熟悉么，一直在用，所以话不多说，先看看`getSpringFactoriesLoaderFactoryClass`返回了一个什么类。返回的是`EnableAutoConfiguration.class;` 所以进入配置文件查看。

```xml
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
org.springframework.boot.autoconfigure.admin.SpringApplicationAdminJmxAutoConfiguration,\
org.springframework.boot.autoconfigure.aop.AopAutoConfiguration,\
org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration,\
org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration,\
org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration,\
org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration,\
org.springframework.boot.autoconfigure.cloud.CloudServiceConnectorsAutoConfiguration,\
org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration,\
org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration,\
org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration,\
...
...
...
```

你应该会看到这么长长的一串配置，这里就是springboot自动化配置的中心了。我就以aop来展示一下springboot是如何简化spring的配置的。

首先经过我们刚刚的一串逻辑org.springframework.boot.autoconfigure.aop.AopAutoConfiguration,这个类会被加载进容器中，那么这个类，和aop又有啥关系呢。

```java
@Configuration
@ConditionalOnClass({ EnableAspectJAutoProxy.class, Aspect.class, Advice.class,
		AnnotatedElement.class })
@ConditionalOnProperty(prefix = "spring.aop", name = "auto", havingValue = "true", matchIfMissing = true)
public class AopAutoConfiguration {

	@Configuration
	@EnableAspectJAutoProxy(proxyTargetClass = false)
	@ConditionalOnProperty(prefix = "spring.aop", name = "proxy-target-class", havingValue = "false", matchIfMissing = false)
	public static class JdkDynamicAutoProxyConfiguration {

	}

	@Configuration
	@EnableAspectJAutoProxy(proxyTargetClass = true)
	@ConditionalOnProperty(prefix = "spring.aop", name = "proxy-target-class", havingValue = "true", matchIfMissing = true)
	public static class CglibAutoProxyConfiguration {

	}

}
```

查看该类的源码，发现该类加载时有两个判断条件，容器中需要有`EnableAspectJAutoProxy.class, Aspect.class, Advice.class,AnnotatedElement.class`这几个注解，或者有`spring.aop`相关的配置。(关于Conditional条件的机制后面再详细解读，这个地方大概了解一下即可)

如果我们在启动时的类上添加了`EnableAspectJAutoProxy`注解的话，该注解会加载`AspectJAutoProxyRegistrar`类，这个类又会向容器注入`AnnotationAwareAspectJAutoProxyCreator`类，而后者正是aop的核心类。只要这个类进入容器，容器就带有了aop功能。

```java
@Import(AspectJAutoProxyRegistrar.class)
public @interface EnableAspectJAutoProxy {}
```

那么如果我没有显示的添加`EnableAspectJAutoProxy`注解会怎样呢？如果没有显示添加的话，只要满足其他条件，`AopAutoConfiguration`类依然会被加载进容器，而他进入容器后，里面得到两个静态类也会被扫描进容器，而这两个类都是带有`EnableAspectJAutoProxy`注解的，所以aop功能依然可以实现。

所以当我们获得了自动化配置的这些支持后，就该回到刚刚标记的`processImport`方法了。

```java
	public void processGroupImports() {
		for (DeferredImportSelectorGrouping grouping : this.groupings.values()) {
			grouping.getImports().forEach(entry -> {
				ConfigurationClass configurationClass = this.configurationClasses.get(
						entry.getMetadata());
				try {
				//刚刚标记的方法
					processImports(configurationClass, asSourceClass(configurationClass),
							asSourceClasses(entry.getImportClassName()), false);
				}
				catch (BeanDefinitionStoreException ex) {
					throw ex;
				}
				catch (Throwable ex) {
					throw new BeanDefinitionStoreException(
							"Failed to process import candidates for configuration class [" +
									configurationClass.getMetadata().getClassName() + "]", ex);
				}
			});
		}
	}
```

这个方法会把我们获得的自动化配置相关支持全部导入容器，这样在经过spring那一套加载逻辑之后，我们的springboot项目就可以获得各种我们配置的功能了。
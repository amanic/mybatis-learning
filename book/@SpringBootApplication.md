# 深入了解Springboot

下面是`springboot`的启动入口：

```java
@SpringBootApplication
public class SpringBootWebDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringBootWebDemoApplication.class, args);
    }
}
```

## @SpringBootApplication

先说说`@SpringBootApplication`注解,源码如下：

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(excludeFilters = { @Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class),
      @Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class) })
public @interface SpringBootApplication {...}
```

主要分为三个注解：`@SpringBootConfiguration`、`@EnableAutoConfiguration`、`@ComponentScan`

### @SpringBootConfiguration

`@SpringBootConfiguration`其实就是包了一下`@Configuration`注解，`@Configuration`是一个类级注释，指示对象是一个bean定义的源。`@Configuration`类通过 `@bean` 注解的公共方法声明bean。

`@Bean`注释是用来表示一个方法实例化，配置和初始化是由 Spring IoC 容器管理的一个新的对象。

通俗的讲 `@Configuration` 一般与 `@Bean` 注解配合使用，用 `@Configuration` 注解类等价与 XML 中配置 beans，用 `@Bean` 注解方法等价于 XML 中配置 bean。举例说明：

xml代码如下：

```xml
<beans>
    <bean id = "userService" class="com.user.UserService">
        <property name="userDAO" ref = "userDAO"></property>
    </bean>
    <bean id = "userDAO" class="com.user.UserDAO"></bean>
</beans>
```

等价于

```java
package org.spring.com.user;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {
    @Bean
    public UserService getUserService(){
        UserService userService = new UserService();
        userService.setUserDAO(null);
        return userService;
    }

    @Bean
    public UserDAO getUserDAO(){
        return new UserDAO();
    }
}
```

### @ComponentScan

使用过spring框架的小伙伴都知道，spring里有四大注解：`@Service`,`@Repository`,`@Component`,`@Controller`用来定义一个bean.`@ComponentScan`注解就是用来自动扫描被这些注解标识的类，最终生成ioc容器里的bean．可以通过设置`@ComponentScan`　basePackages，includeFilters，excludeFilters属性来动态确定自动扫描范围，类型已经不扫描的类型．默认情况下:它扫描所有类型，并且扫描范围是`@ComponentScan`注解所在配置类包及子包的类。使用`@SpringBootApplication`注解，就说明你使用了`@ComponentScan`的默认配置，这就建议你把使用`@SpringBootApplication`注解的类放置在root package(官方表述)下，其他类都置在root package的子包里面，这样bean就不会被漏扫描．

### @EnableAutoConfiguration

#### @Import

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Import {

   /**
    * {@link Configuration}, {@link ImportSelector}, {@link ImportBeanDefinitionRegistrar}
    * or regular component classes to import. 以下一一都有例子
    */
   Class<?>[] value();

}
```

@Import其实就是引入一个或多个配置，可以导入普通类，也可以导入配置类。
@Import用来导入一个或多个类（会被spring容器管理），或者配置类（配置类里的@Bean标记的类也会被spring容器管理）。

看一个demo，定义四个实体类，User，People，Cat，Dog。

```java
public class User {
}
public class People{  
}
public class Cat {
}
public class Dog {
}
```

```java
public class MyConfig {

    @Bean
    public Dog dog(){
        return new Dog();
    }

    @Bean
    public Cat cat(){
        return new Cat();
    }
}
```

##### 例子一

**Configuration or regular component**

我们要将这四个类纳入到spring容器中，我们之前的做法是在User，People上加上了@Component注解(或者@Service，@Controller）或者在MyConfig类上加上@Configuration注解。很显然我们这边并没有这般做，使用@Import注解也可以加对象纳入到spring容器中。

启动类：

```java
@Import({User.class,People.class, MyConfig.class})
public class Application {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class,args);
        System.out.println(context.getBean(User.class));
        System.out.println(context.getBean(Dog.class));
        System.out.println(context.getBean(Cat.class));
        System.out.println(context.getBean(People.class));
    }
}
```

##### 例子二 ImportSelector配合Enable*

```java
/**
 * Interface to be implemented by types that determine which @{@link Configuration}
 * class(es) should be imported based on a given selection criteria, usually one or
 * more annotation attributes.
 *
 * <p>An {@link ImportSelector} may implement any of the following
 * {@link org.springframework.beans.factory.Aware Aware} interfaces,
 * and their respective methods will be called prior to {@link #selectImports}:
 * <ul>
 * <li>{@link org.springframework.context.EnvironmentAware EnvironmentAware}</li>
 * <li>{@link org.springframework.beans.factory.BeanFactoryAware BeanFactoryAware}</li>
 * <li>{@link org.springframework.beans.factory.BeanClassLoaderAware BeanClassLoaderAware}</li>
 * <li>{@link org.springframework.context.ResourceLoaderAware ResourceLoaderAware}</li>
 * </ul>
 *
 * <p>{@code ImportSelector} implementations are usually processed in the same way
 * as regular {@code @Import} annotations, however, it is also possible to defer
 * selection of imports until all {@code @Configuration} classes have been processed
 * (see {@link DeferredImportSelector} for details).
 */
public interface ImportSelector {

   /**
    * Select and return the names of which class(es) should be imported based on
    * the {@link AnnotationMetadata} of the importing @{@link Configuration} class.
    */
   String[] selectImports(AnnotationMetadata importingClassMetadata);

}
```

应该被实现了Configuration接口并且根据指定的一些条件应当被导入到Spring容器的类实现。

```java
/**
 * selectImports方法的返回值，必须是一个class（全称），该class会被spring容器所托管起来
 */
public class MyImportSelector implements ImportSelector{

    @Override
    public String[] selectImports(AnnotationMetadata metadata) {
        //获取注解的属性信息
 				System.out.println(metadata.getAllAnnotationAttributes(EnableBean.class.getName()));
       //这里可以获取到注解的详细信息，然后根据信息去动态的返回需要被spring容器管理的bean
        return new String[] {User.class.getName(),People.class.getName(),MyConfig.class.getName()};
    }

}
```

定义一个EnableLog注解，可以得到属性的值，@Import(MyImportSelector.class)，可以在MyImportSelector中获取name属性值。

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(MyImportSelector.class)
public @interface EnableBean {
    String name();
}
```

启动类，在启动类上加入`@EnableBean(name="myName")`注解，
我们知道`@EnableBean`中`@Import(MyImportSelector.class)`会将`MyImportSelector`对象纳入到容器中。

```java
@EnableLog(name="myName")
public class Application {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class,args);
        System.out.println(context.getBean(User.class));
        System.out.println(context.getBean(Dog.class));
        System.out.println(context.getBean(Cat.class));
        System.out.println(context.getBean(People.class));
    }

}
```

输出

```java
{name=[myName]}
2017-07-20 11:27:08.446  INFO 11551 --- [           main] Application:Started Application2 in 11.754 seconds (JVM running for 12.614)
User@1e4d3ce5
Dog@3ddc6915
Cat@704deff2
People@379614be
```

其实这里不配合`@EnableBean`，直接在`Application`上面加上`@Import(MyImportSelector.class)`也可以。

##### 例子三 ImportBeanDefinitionRegistrar接口配合@Enable*

```java
/**
 * 接口实现可以额外的注册类的定义到spring容器中。
 */
public interface ImportBeanDefinitionRegistrar {

   /**
    * Register bean definitions as necessary based on the given annotation metadata of
    * the importing {@code @Configuration} class.
    * <p>Note that {@link BeanDefinitionRegistryPostProcessor} types may <em>not</em> be
    * registered here, due to lifecycle constraints related to {@code @Configuration}
    * class processing.
    * @param importingClassMetadata annotation metadata of the importing class
    * @param registry current bean definition registry
    */
   void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry);
}
```

```java
/**
 * registerBeanDefinitions方法的参数有一个BeanDefinitionRegistry，
 * BeanDefinitionRegistry可以用来往spring容器中注入bean
 * 如此，我们就可以在registerBeanDefinitions方法里面动态的注入bean
 */
public class MyImportBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        BeanDefinitionBuilder bdb = BeanDefinitionBuilder.rootBeanDefinition(People.class);
        registry.registerBeanDefinition(People.class.getName(),bdb.getBeanDefinition());

        BeanDefinitionBuilder bdb2 = BeanDefinitionBuilder.rootBeanDefinition(User.class);
        registry.registerBeanDefinition(User.class.getName(),bdb2.getBeanDefinition());

        BeanDefinitionBuilder bdb3 = BeanDefinitionBuilder.rootBeanDefinition(MyConfig.class);
        registry.registerBeanDefinition(MyConfig.class.getName(),bdb3.getBeanDefinition());
    }
}
```

主启动类：

```java
@Import(MyImportBeanDefinitionRegistrar.class)
public class Application {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class,args);
        System.out.println(context.getBean(User.class));
        System.out.println(context.getBean(Dog.class));
        System.out.println(context.getBean(Cat.class));
        System.out.println(context.getBean(People.class));
    }
}
```

打印结果：

```css
...
User@3e694b3f
Dog@1bb5a082
Cat@78691363
People@41d477ed
...
```

当然也可以写成一个注解，@EnableImportConfig

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(MyImportBeanDefinitionRegistrar.class)
public @interface EnableImportConfig {
}
```

启动类：

```java
@EnableImportConfig
public class Application {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class,args);
        System.out.println(context.getBean(User.class));
        System.out.println(context.getBean(Dog.class));
        System.out.println(context.getBean(Cat.class));
        System.out.println(context.getBean(People.class));
    }
}
```

和例子二有点类似，可以`@Import`和`@Enable*`交替使用。也是可以将这些对象注入到spring容器的。

接下来准备查看`SpringBoot`是在何时解析`@Enable`或者`@Import`注解的，其实`@Enable`就是对`@Import`的封装，最主要还是看`@Import`注解。但是一圈看下来，还是要先了解`SpringBoot`的启动过程，所以先看一下`SpringBoot`的启动过程。

通过看代码，终于了解到，`@Import`注解式在`ConfigurationClassParser`这个类里面去分析的。看到这里听一下，有点费头发。。。

## @Import注解的解析

通过上面的介绍，我们知道了@SpringBoot注解的作用原理，并且提到了@Import注解的解析。下面就详细解释一下@Import注解的解析。

@Import注解的解析是在`ConfigurationClassParser`类里面去解析的，

我们先了解一下`ConfigurationClassParser`；

### ConfigurationClassParser

#### 简介

Spring的工具类`ConfigurationClassParser`用于分析`@Configuration`注解的配置类，产生一组`ConfigurationClass`对象。它的分析过程会接受一组种子配置类(调用者已知的配置类，通常很可能只有一个)，从这些种子配置类开始分析所有关联的配置类，分析过程主要是递归分析配置类的注解`@Import`，配置类内部嵌套类，找出其中所有的配置类，然后返回这组配置类。

该工具主要由`ConfigurationClassPostProcessor`使用，而`ConfigurationClassPostProcessor`是一个`BeanDefinitionRegistryPostProcessor/BeanFactoryPostProcessor`,它会在容器启动过程中，应用上下文上执行各个`BeanFactoryPostProcessor`时被执行。

这个工具类自身的逻辑并不注册bean定义，它的主要任务是发现`@Configuration`注解的所有配置类并将这些配置类交给调用者(调用者会通过其他方式注册其中的bean定义)，而对于非`@Configuration`注解的其他bean定义，比如`@Component`注解的bean定义，该工具类使用另外一个工具`ComponentScanAnnotationParser`扫描和注册它们。该工具类对`@ComponentScans`,`@ComponentScan`注解的处理使用了`ComponentScanAnnotationParser`，`ComponentScanAnnotationParser`在扫描到bean定义时会直接将其注册到容器，而不是采用和`ConfigurationClassParser`类似的方式交由调用者处理。

一般情况下一个`@Configuration`注解的类只会产生一个`ConfigurationClass`对象，但是因为`@Configuration`注解的类可能会使用注解`@Import`引入其他配置类，也可能内部嵌套定义配置类，所以总的来看，`ConfigurationClassParser`分析一个`@Configuration`注解的类，可能产生任意多个`ConfigurationClass`对象。

#### 主要功能分析

##### `parse()` : 外部调用入口

`parse()`方法的主体工作流程 :

接收外部提供的参数 `configCandidates` , 是一组需要被分析的候选配置类的集合，每个元素使用类型`BeanDefinitionHolder`包装 ;
parse() 方法针对每个候选配置类元素`BeanDefinitionHolder`，执行以下逻辑 :

1. 将其封装成一个`ConfigurationClass`
2. 调用`processConfigurationClass(ConfigurationClass configClass)`分析过的每个配置类都被保存到属性 `this.configurationClasses` 中。

```java
ConfigurationClass.class
/**
 * @参数 configCandidates : 外部指定需要被分析的一组候选配置类
 **/
public void parse(Set<BeanDefinitionHolder> configCandidates) {
	this.deferredImportSelectors = new LinkedList<DeferredImportSelectorHolder>();

	for (BeanDefinitionHolder holder : configCandidates) {
		BeanDefinition bd = holder.getBeanDefinition();
		try {
			// 这里根据Bean定义的不同类型走不同的分支，但是最终都会调用到方法
			//  processConfigurationClass(ConfigurationClass configClass)
			if (bd instanceof AnnotatedBeanDefinition) {
				// bd 是一个 AnnotatedBeanDefinition
				parse(((AnnotatedBeanDefinition) bd).getMetadata(), holder.getBeanName());
			}
			else if (bd instanceof AbstractBeanDefinition && ((AbstractBeanDefinition) bd).hasBeanClass()) {
				// bd 是一个 AbstractBeanDefinition,并且指定 beanClass 属性
				parse(((AbstractBeanDefinition) bd).getBeanClass(), holder.getBeanName());
			}
			else {
				// 其他情况
				parse(bd.getBeanClassName(), holder.getBeanName());
			}
		}
		catch (BeanDefinitionStoreException ex) {
			throw ex;
		}
		catch (Throwable ex) {
			throw new BeanDefinitionStoreException(
					"Failed to parse configuration class [" + bd.getBeanClassName() + "]", ex);
		}
	}

	// 执行找到的 DeferredImportSelector 
	//  DeferredImportSelector 是 ImportSelector 的一个变种。
	// ImportSelector 被设计成其实和@Import注解的类同样的导入效果，但是实现 ImportSelector
	// 的类可以条件性地决定导入哪些配置。
	// DeferredImportSelector 的设计目的是在所有其他的配置类被处理后才处理。这也正是
	// 该语句被放到本函数最后一行的原因。
	processDeferredImportSelectors();
}
```

##### `processConfigurationClass()` 分析一个配置类

`processConfigurationClass()`对配置的处理并不是真正自己处理，而是开始一个基于`doProcessConfigurationClass()`的处理循环，该循环从参数配置类开始遍历其所有需要处理的父类(super)，每个类都使用`doProcessConfigurationClass()`来处理。每处理一个类，`processConfigurationClass()`将其记录到`this.configurationClasses`。

```java
/**
 *  用于分析一个 ConfigurationClass，分析之后将它记录到已处理配置类记录,由上面代码具体的parse调用
 **/
protected void processConfigurationClass(ConfigurationClass configClass) throws IOException {
	if (this.conditionEvaluator.shouldSkip(configClass.getMetadata(), ConfigurationPhase.PARSE_CONFIGURATION)) {
		return;
	}

	ConfigurationClass existingClass = this.configurationClasses.get(configClass);
	if (existingClass != null) {
		if (configClass.isImported()) {
			if (existingClass.isImported()) {
				//如果要处理的配置类configClass在已经分析处理的配置类记录中已存在，
				//合并二者的importedBy属性
				existingClass.mergeImportedBy(configClass);
			}
			// Otherwise ignore new imported config class; existing non-imported class overrides it.
			return;
		}
		else {
			// Explicit bean definition found, probably replacing an import.
			// Let's remove the old one and go with the new one.
			this.configurationClasses.remove(configClass);
			for (Iterator<ConfigurationClass> it = this.knownSuperclasses.values().iterator(); it.hasNext();) {
				if (configClass.equals(it.next())) {
					it.remove();
				}
			}
		}
	}

	// Recursively process the configuration class and its superclass hierarchy.
	// 从当前配置类configClass开始向上沿着类继承结构逐层执行doProcessConfigurationClass,
	// 直到遇到的父类是由Java提供的类结束循环
	SourceClass sourceClass = asSourceClass(configClass);
	do {			
		// 循环处理配置类configClass直到sourceClass变为null
		// doProcessConfigurationClass的返回值是其参数configClass的父类，
		// 如果该父类是由Java提供的类或者已经处理过，返回null
		sourceClass = doProcessConfigurationClass(configClass, sourceClass);
	}
	while (sourceClass != null);

	// 需要被处理的配置类configClass已经被分析处理，将它记录到已处理配置类记录
	this.configurationClasses.put(configClass, configClass);
}
```

##### `doProcessConfigurationClass()`对一个配置类执行真正的处理

`doProcessConfigurationClass()`会对一个配置类执行真正的处理：

1. 一个配置类的成员类(配置类内嵌套定义的类)也可能适配类，先遍历这些成员配置类，调用`processConfigurationClass`处理它们;
2. 处理配置类上的注解`@PropertySources`,`@PropertySource`
3. 处理配置类上的注解`@ComponentScans`,`@ComponentScan`
4. 处理配置类上的注解`@Import`
5. 处理配置类上的注解`@ImportResource`
6. 处理配置类中每个带有`@Bean`注解的方法
7. 处理配置类所实现接口的缺省方法
8. 检查父类是否需要处理，如果父类需要处理返回父类，否则返回null

返回父类表示当前配置类处理尚未完成，调用者`processConfigurationClass`会继续处理其父类；返回null才表示该配置类的处理完成。从这里可以推断一旦一个配置类被`processConfigurationClass`处理完成，表示其自身，内部嵌套类，各个实现接口以及各级父类都被处理完成。

```java
/**
 * Apply processing and build a complete ConfigurationClass by reading the
 * annotations, members and methods from the source class. This method can be called
 * multiple times as relevant sources are discovered.
 * @param configClass the configuration class being build
 * @param sourceClass a source class
 * @return the superclass, or null if none found or previously processed
 */
protected final SourceClass doProcessConfigurationClass(ConfigurationClass configClass, SourceClass sourceClass)
		throws IOException {

	// Recursively process any member (nested) classes first，首先递归处理嵌套类
	processMemberClasses(configClass, sourceClass);

	// Process any @PropertySource annotations，处理每个@PropertySource注解
	for (AnnotationAttributes propertySource : AnnotationConfigUtils.attributesForRepeatable(
			sourceClass.getMetadata(), PropertySources.class,
			org.springframework.context.annotation.PropertySource.class)) {
		if (this.environment instanceof ConfigurableEnvironment) {
			processPropertySource(propertySource);
		}
		else {
			logger.warn("Ignoring @PropertySource annotation on [" + sourceClass.getMetadata().getClassName() +
					"]. Reason: Environment must implement ConfigurableEnvironment");
		}
	}

	// Process any @ComponentScan annotations，处理每个@ComponentScan注解
	Set<AnnotationAttributes> componentScans = AnnotationConfigUtils.attributesForRepeatable(
			sourceClass.getMetadata(), ComponentScans.class, ComponentScan.class);
	if (!componentScans.isEmpty() &&
			!this.conditionEvaluator.shouldSkip(sourceClass.getMetadata(), ConfigurationPhase.REGISTER_BEAN)) {
		for (AnnotationAttributes componentScan : componentScans) {
			// 该配置类上注解了@ComponentScan,现在执行扫描，获取其中的Bean定义
			// this.componentScanParser 是一个 ComponentScanAnnotationParser，在当前对象的构造函数中
			// 被创建
			Set<BeanDefinitionHolder> scannedBeanDefinitions =
					this.componentScanParser.parse(componentScan, sourceClass.getMetadata().getClassName());
			// 对Component scan得到的Bean定义做检查，看看里面是否有需要处理的配置类，
			// 有的话对其做分析处理
			for (BeanDefinitionHolder holder : scannedBeanDefinitions) {
				if (ConfigurationClassUtils.checkConfigurationClassCandidate(
						holder.getBeanDefinition(), this.metadataReaderFactory)) {
					// 如果该Bean定义是一个配置类，它进行分析
					parse(holder.getBeanDefinition().getBeanClassName(), holder.getBeanName());
				}
			}
		}
	}

	// Process any @Import annotations，处理每个@Import注解		
	// 注意这里调用到了getImports()方法，它会搜集sourceClass上所有的@Import注解的value值，
	// 具体搜集的方式是访问sourceClass直接注解的@Import以及递归访问它的注解中隐含的所有的@Import	
	processImports(configClass, sourceClass, getImports(sourceClass), true);

	// Process any @ImportResource annotations，处理每个@ImportResource注解
	if (sourceClass.getMetadata().isAnnotated(ImportResource.class.getName())) {
		AnnotationAttributes importResource =
				AnnotationConfigUtils.attributesFor(sourceClass.getMetadata(), ImportResource.class);
		String[] resources = importResource.getStringArray("locations");
		Class<? extends BeanDefinitionReader> readerClass = importResource.getClass("reader");
		for (String resource : resources) {
			String resolvedResource = this.environment.resolveRequiredPlaceholders(resource);
			configClass.addImportedResource(resolvedResource, readerClass);
		}
	}

	// Process individual @Bean methods,处理配置类中每个带有@Bean注解的方法
	Set<MethodMetadata> beanMethods = retrieveBeanMethodMetadata(sourceClass);
	for (MethodMetadata methodMetadata : beanMethods) {
		configClass.addBeanMethod(new BeanMethod(methodMetadata, configClass));
	}

	// Process default methods on interfaces，处理接口上的缺省方法
	processInterfaces(configClass, sourceClass);

	// Process superclass, if any
	// 如果父类superclass存在，并且不是`java`包中的类，并且尚未处理处理，
	// 则才返回它以便外层循环继续
	if (sourceClass.getMetadata().hasSuperClass()) {
		String superclass = sourceClass.getMetadata().getSuperClassName();
		if (!superclass.startsWith("java") && !this.knownSuperclasses.containsKey(superclass)) {
			this.knownSuperclasses.put(superclass, configClass);
			// Superclass found, return its annotation metadata and recurse
			return sourceClass.getSuperClass();
		}
	}

	// No superclass -> processing is complete，没找到需要处理的父类，处理结果
	return null;// 用返回null告诉外层循环结束
}
```

##### `processImports()`处理配置类上搜集到的`@Import`注解

```java
/**
  * 处理配置类上搜集到的@Import注解
  * 参数 configuClass 配置类
  * 参数 currentSourceClass 当前源码类
  * 参数 importCandidates, 所有的@Import注解的value
  * 参数 checkForCircularImports, 是否检查循环导入
  **/
private void processImports(ConfigurationClass configClass, SourceClass currentSourceClass,
		Collection<SourceClass> importCandidates, boolean checkForCircularImports) throws IOException {

	if (importCandidates.isEmpty()) {
	// 如果配置类上没有任何候选@Import，说明没有需要处理的导入，则什么都不用做，直接返回
		return;
	}
				
	if (checkForCircularImports && isChainedImportOnStack(configClass)) {
	// 如果要求做循环导入检查，并且检查到了循环依赖，报告这个问题
		this.problemReporter.error(new CircularImportProblem(configClass, this.importStack));
	}
	else {
		// 开始处理配置类configClass上所有的@Import importCandidates
		this.importStack.push(configClass);
		try {
			// 循环处理每一个@Import,每个@Import可能导入三种类型的类 :
			// 1. ImportSelector
			// 2. ImportBeanDefinitionRegistrar
			// 3. 其他类型，都当作配置类处理，也就是相当于使用了注解@Configuration的配置类
			// 下面的for循环中对这三种情况执行了不同的处理逻辑
			for (SourceClass candidate : importCandidates) {
				if (candidate.isAssignable(ImportSelector.class)) {
					// Candidate class is an ImportSelector -> delegate to it to determine imports
					Class<?> candidateClass = candidate.loadClass();
					ImportSelector selector = BeanUtils.instantiateClass(candidateClass, ImportSelector.class);
					ParserStrategyUtils.invokeAwareMethods(
							selector, this.environment, this.resourceLoader, this.registry);
					if (this.deferredImportSelectors != null && selector instanceof DeferredImportSelector) {
						this.deferredImportSelectors.add(
								new DeferredImportSelectorHolder(configClass, (DeferredImportSelector) selector));
					}
					else {
						String[] importClassNames = selector.selectImports(currentSourceClass.getMetadata());
						Collection<SourceClass> importSourceClasses = asSourceClasses(importClassNames);
						processImports(configClass, currentSourceClass, importSourceClasses, false);
					}
				}
				else if (candidate.isAssignable(ImportBeanDefinitionRegistrar.class)) {
					// Candidate class is an ImportBeanDefinitionRegistrar ->
					// delegate to it to register additional bean definitions
					Class<?> candidateClass = candidate.loadClass();
					ImportBeanDefinitionRegistrar registrar =
							BeanUtils.instantiateClass(candidateClass, ImportBeanDefinitionRegistrar.class);
					ParserStrategyUtils.invokeAwareMethods(
							registrar, this.environment, this.resourceLoader, this.registry);
					configClass.addImportBeanDefinitionRegistrar(registrar, currentSourceClass.getMetadata());
				}
				else {
					// Candidate class not an ImportSelector or ImportBeanDefinitionRegistrar ->
					// process it as an @Configuration class
					this.importStack.registerImport(
							currentSourceClass.getMetadata(), candidate.getMetadata().getClassName());
					processConfigurationClass(candidate.asConfigClass(configClass));
				}
			}
		}
		catch (BeanDefinitionStoreException ex) {
			throw ex;
		}
		catch (Throwable ex) {
			throw new BeanDefinitionStoreException(
					"Failed to process import candidates for configuration class [" +
					configClass.getMetadata().getClassName() + "]", ex);
		}
		finally {
			this.importStack.pop();
		}
	}
}
```

##### `processDeferredImportSelectors()` 处理需要延迟处理的`ImportSelector`

```java
	/**
	  * 对属性deferredImportSelectors中记录的DeferredImportSelector进行处理
      **/
	private void processDeferredImportSelectors() {
		List<DeferredImportSelectorHolder> deferredImports = this.deferredImportSelectors;
		this.deferredImportSelectors = null;
		Collections.sort(deferredImports, DEFERRED_IMPORT_COMPARATOR);
		
		// 循环处理每个DeferredImportSelector
		for (DeferredImportSelectorHolder deferredImport : deferredImports) {
			ConfigurationClass configClass = deferredImport.getConfigurationClass();
			try {
				//调用DeferredImportSelector的方法selectImports,获取需要被导入的类的名称
				String[] imports = deferredImport.getImportSelector().selectImports(configClass.getMetadata());
				// 处理任何一个 @Import 注解
				processImports(configClass, asSourceClass(configClass), asSourceClasses(imports), false);
			}
			catch (BeanDefinitionStoreException ex) {
				throw ex;
			}
			catch (Throwable ex) {
				throw new BeanDefinitionStoreException(
						"Failed to process import candidates for configuration class [" +
						configClass.getMetadata().getClassName() + "]", ex);
			}
		}
	}
```

至此，@SpringBootApplication注解的介绍以及解析结束，如果还有疑问，接下来要看SpringBoot的启动流程了。
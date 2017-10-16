# Idea Plugin
---
## <font color="green">Prepare</font>
- Project JDK: 1.7+
- Gradle: 3.0+（Require JDK1.8+ for gradle）

## <font color="green">Build</font>
```
cd p3c-idea
gradle clean buildPlugin
```

## <font color="green">Run plugin</font>

```
cd p3c-idea
gradle runIde
# run specific IDEA
gradle runIde -Pidea_version=14.1.7
```

## <font color="green">Use p3c-common as your plugin dependency</font>
``` groovy
compile 'com.alibaba.p3c.idea:p3c-common:1.0.0'
```

## <font color="green">Install</font>

1. <font color="blue">Settings >> Plugins >> Browse repositories... </font>

    ![Switch language](doc/images/install_1.png) 

2. <font color="blue"> Search plugin by keyword 'alibaba' then install 'Alibaba Java Coding Guidelines' plugin </font>

    ![Switch language](doc/images/install_2.png) 

3.  <font color="blue">Restart to take effect. </font>

## <font color="green">Use</font>

1. <font color="blue">Switch language</font>

	![Switch language](doc/images/switch_language.png) 

2. <font color="blue">Inspections</font>  

	![Real time](doc/images/inspection.png) 
	
	![Settings](doc/images/inspection_setting.png)  

3. <font color="blue">Code Analyze</font>  

	![Settings](doc/images/analyze.png)  
	
	<font color="blue">We use the idea standard Inspection Results to show our violations.</font>  
	 
	![Result](doc/images/inspection_result.png)  
	
	<font color="blue">We can also analyze file which is modified before vcs checkin.</font>  
	
	![Before Checkin](doc/images/analyze_before_checkin.png) 
	

# Idea Plugin 
---
## <font color="green">Prepare</font>
- Project JDK: 11
- Gradle: 6.8.3（Require JDK11 for gradle）

## <font color="green">Build</font>
```
cd p3c-idea
../gradlew clean buildPlugin
```

## <font color="green">Run plugin</font>

```
cd p3c-idea
../gradlew runIde
# run specific IDEA
../gradlew runIde -Pidea_version=2018.3
```

## <font color="green">Use p3c-common as your plugin dependency</font>
```groovy
compile 'com.xenoamess.p3c.idea:p3c-common:2.1.1.5x-SNAPSHOT'
```
## [中文使用手册](README_cn.md)
## <font color="green">Install</font>
### Install from repositories
1. <font color="blue">Settings >> Plugins >> Browse repositories... </font>

    ![Switch language](doc/images/install_1.png) 

2. <font color="blue"> Search plugin by keyword 'alibaba' then install 'Alibaba Java Coding Guidelines' plugin </font>

    ![Switch language](doc/images/install_2.png) 

3.  <font color="blue">Restart to take effect. </font>
### Install from local zip file.
1. Open https://plugins.jetbrains.com/plugin/10046-alibaba-java-coding-guidelines and download the latest version zip file.
    ![download](https://gw.alicdn.com/tfscom/TB1WcF3hzlxYKJjSZFuXXaYlVXa.png)
2. Settings >> Plugins >> Install plugin from disk...,select the downloaded zip file in previous step then restart your idea
    ![](https://gw.alicdn.com/tfscom/TB1WFsKiqigSKJjSsppXXabnpXa.png)

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

Results are grouped by tags, which include Blocker,Critical,Major,Warning,Weak Warning.

Blocker, Critical and Major will remain sync with alibaba official version, and will not be changed freely, unless especially declared.

When I see some reasonable third party rules in Community I might add them to Warning or Weak Warning.

Usually, if a third party rule can be followed in every situation, then it is tagged Warning.

Otherwise, if it cannot be followed in some special cases, then it will be tagged Weak Warning.

If you want to use pmd-maven-plugin for auto-analyze and assure, 

You should set `<failurePriority>3</failurePriority>` for having same behavior than alibaba official version.

See example/use-case in `p3c-pmd/pom.xml`

	
	<font color="blue">We can also analyze file which is modified before vcs checkin.</font>  
	
	![Before Checkin](doc/images/analyze_before_checkin.png) 

## <font color="green">Other</font>
1. <font color="blue">[中文乱码解决方法](https://github.com/alibaba/p3c/issues/32#issuecomment-336762512)</font>

	* <font color="blue">Appearance&Behavior -> Appearance -> UI Options -> Name 里面设置成微软雅黑（microsoft yahei light）</font>

	   ![Font](doc/images/change_name.png) 
 
	* <font color="blue">Switch Language to English and restart.</font>

	   ![Switch language](doc/images/normal_view.png) 

## Configuration mechanism
### Configuration file path

For p3c-idea-plugin, configuration file should be put at "$your_project_path/p3c_config.x8l".

For p3c-pmd for maven, configuration file should be put at "$the_dir_you_run_mvn/p3c_config.x8l".

Usually this two position be pointed at a same file.

### File format

File format be [x8l](https://github.com/cyanpotion/x8l).
Yep I write it.

### File analyze

For example at [cyan_potion](https://github.com/cyanpotion/cyan_potionp3c_config.x8l) ,
we learn about how it works.

Firstly p3c_config.x8l be at "$your_project_path/p3c_config.x8l".

```
<com.alibaba.p3c.pmd.config version=0.0.1>
    <rule_config>
        <LowerCamelCaseVariableNamingRule>
            <WHITE_LIST [>
                DAOImpl&
                GLFW&
                URL&
                URI&
                XInput&
                PosX&
                PosY&
                AWT&
                XY&
                drawBoxTC&
                FPS&
                ID&
                lastX&
                lastY&
            >
        >
        <ClassNamingShouldBeCamelRule>
            <CLASS_NAMING_WHITE_LIST [>
                Hbase&
                HBase&
                ID&
                ConcurrentHashMap&
                GLFW&
                URL&
                URI&
                JXInput&
                SettingFileParser_
            >
        >
    >
    <rule_blacklist [>
        PackageNamingRule&
        AbstractClassShouldStartWithAbstractNamingRule&
        ThreadPoolCreationRule&
        MethodTooLongRule&
    >
    <class_blacklist [>
        Console
    >
	<package_blacklist [>
        com.xenoamess.cyan_potion.base.steam
    >
    <rule_class_pair_blacklist>
        <JamepadGamepadKeyEnum [>EnumConstantsMustHaveCommentRule>
        <JXInputGamepadKeyEnum [>EnumConstantsMustHaveCommentRule>
        <KeyActionEnum [>EnumConstantsMustHaveCommentRule>
        <KeyboardKeyEnum [>EnumConstantsMustHaveCommentRule>
        <CodePluginPosition [>EnumConstantsMustHaveCommentRule>
        <ShapeRelation [>EnumConstantsMustHaveCommentRule>

        <WaveData [>UndefineMagicConstantRule>

        <FileUtils [>AvoidUseDeprecationRule>

        <Font [>AvoidUseDeprecationRule>
        <Keymap [>AvoidUseDeprecationRule>
        <WorldForDemo [>AvoidUseDeprecationRule>

        <GameInputManager [>LowerCamelCaseVariableNamingRule&AvoidUseDeprecationRule>

        <Colors [>ConstantFieldShouldBeUpperCaseRule>
    >
>
```

Root node's name MUST be "com.alibaba.p3c.pmd.config".

Attribute "version=0.0.1" is version of configuration file.
It is actually not used yet but stongly suggested force it here,
as for breaking updates.

For this repo, we opened trim for x8l file, means all readed String will be trimed first.

For example,

```
    <class_blacklist>
        Console
    >
```

see the TextNode

```

        Console
    
```

It will be trimmed to `Console` before used.

There can be 4 children nodes under node com.alibaba.p3c.pmd.config, as shown below.

### rule_config

Node rule_config contains detailed settings for some rules.

For example, LowerCamelCaseVariableNamingRule's attribute WHITE_LIST.

This attribute need a String List,
then we read several TextNodes under a ContentNode as a String List.

### rule_blacklist

Node rule_blacklist contains global settings for a repo,
means ban rules in this repo globally.

For example, if rule_blacklist contains PackageNamingRule,
then means in this repo we will not detect PackageNamingRule.

Rule class name in rule_blacklist CAN be SimpleName OR CanonicalName.

### class_blacklist

Node class_blacklist contains global settings for a repo,
means ban classes in this repo globally.

For example, if class_blacklist contains Console,
then means in this repo we will not detect anything for all classes whose name be Console.

BE ATTENTION, according to PMD interface reason,
class names in class_blacklist must be SimpleName.

### package_blacklist

Node package_blacklist contains global settings for a repo,
means ban packages in this repo globally.

For example, if package_blacklist contains `com.xenoamess`,
then means in this repo we will not detect anything for all classes whose package under `com.xenoamess`.

BE ATTENTION, it will also ban packages under `com.xenoamess`, like `com.xenoamess.cyan_potion`

### rule_class_pair_blacklist

Node rule_class_pair_blacklist contains settings for class/rule pairs,
means ban some rules in this repo for some classes.

For example, if rule_class_pair_blacklist contains
`<GameInputManager>LowerCamelCaseVariableNamingRule&AvoidUseDeprecationRule>`
,
then means in this repo we will not detect LowerCamelCaseVariableNamingRule nor AvoidUseDeprecationRule,
for all classes whose name be GameInputManager.


Rule class name in rule_class_pair_blacklist CAN be SimpleName OR CanonicalName.

BE ATTENTION, according to PMD interface reason,
class names in rule_class_pair_blacklist must be SimpleName.

### For the X8L Haters

If you really hate x8l you can use json configuration files.

```json
{
  "com.alibaba.p3c.pmd.config": {
    "_attributes": {
      "version": "0.0.1"
    },
    "rule_config": {
      "LowerCamelCaseVariableNamingRule": {
        "WHITE_LIST": [
          "DAOImpl",
          "GLFW",
          "URL",
          "URI",
          "XInput",
          "PosX",
          "PosY",
          "AWT",
          "XY",
          "drawBoxTC",
          "FPS",
          "ID",
          "lastX",
          "lastY"
        ]
      },
      "ClassNamingShouldBeCamelRule": {
        "CLASS_NAMING_WHITE_LIST": [
          "Hbase",
          "HBase",
          "ID",
          "ConcurrentHashMap",
          "GLFW",
          "URL",
          "URI",
          "JXInput",
          "SettingFileParser_"
        ]
      }
    },
    "rule_blacklist": [
      "PackageNamingRule",
      "AbstractClassShouldStartWithAbstractNamingRule",
      "ThreadPoolCreationRule",
      "MethodTooLongRule"
    ],
    "class_blacklist": [
      "Console"
    ], 
	"package_blacklist": [
	  "com.xenoamess.cyan_potion.base.steam"
	],
    "rule_class_pair_blacklist": {
      "JamepadGamepadKeyEnum": [
        "EnumConstantsMustHaveCommentRule"
      ],
      "JXInputGamepadKeyEnum": [
        "EnumConstantsMustHaveCommentRule"
      ],
      "KeyActionEnum": [
        "EnumConstantsMustHaveCommentRule"
      ],
      "KeyboardKeyEnum": [
        "EnumConstantsMustHaveCommentRule"
      ],
      "CodePluginPosition": [
        "EnumConstantsMustHaveCommentRule"
      ],
      "ShapeRelation": [
        "EnumConstantsMustHaveCommentRule"
      ],
      "WaveData": [
        "UndefineMagicConstantRule"
      ],
      "FileUtils": [
        "AvoidUseDeprecationRule"
      ],
      "Font": [
        "AvoidUseDeprecationRule"
      ],
      "Keymap": [
        "AvoidUseDeprecationRule"
      ],
      "WorldForDemo": [
        "AvoidUseDeprecationRule"
      ],
      "GameInputManager": [
        "LowerCamelCaseVariableNamingRule",
        "AvoidUseDeprecationRule"
      ],
      "Colors": [
        "ConstantFieldShouldBeUpperCaseRule"
      ]
    }
  }
}
```

name it p3c_config.json

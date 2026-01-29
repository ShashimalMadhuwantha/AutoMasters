; GalleAuto Service Installer Script

#define MyAppName "GalleAuto Service"
#define MyAppVersion "1.0"
#define MyAppPublisher "Shashimal Madhuwantha"
#define MyAppExeName "launch.bat"

[Setup]
AppId={{C6497C50-9203-412A-AF4D-C91DF43732E6}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppPublisher={#MyAppPublisher}
DefaultDirName={autopf}\{#MyAppName}
DisableProgramGroupPage=yes
OutputBaseFilename=GalleAutoService-Setup-v1.0
Compression=lzma2/ultra64
SolidCompression=yes
WizardStyle=modern
PrivilegesRequired=lowest
OutputDir=F:\PROJECTS\AutoMasters\installer
SetupIconFile=F:\PROJECTS\AutoMasters\app-icon.ico
UninstallDisplayIcon={app}\app-icon.ico

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"

[Files]
; The main JAR
Source: "F:\PROJECTS\AutoMasters\target\automasters-1.0-SNAPSHOT.jar"; DestDir: "{app}"; DestName: "automasters.jar"; Flags: ignoreversion
; The Launcher
Source: "F:\PROJECTS\AutoMasters\target\launch.bat"; DestDir: "{app}"; Flags: ignoreversion
; The JRE
Source: "F:\PROJECTS\AutoMasters\jre\*"; DestDir: "{app}\jre"; Flags: ignoreversion recursesubdirs createallsubdirs
; The Icon
Source: "F:\PROJECTS\AutoMasters\app-icon.ico"; DestDir: "{app}"; DestName: "app-icon.ico"; Flags: ignoreversion

[Icons]
Name: "{autoprograms}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; IconFilename: "{app}\app-icon.ico"
Name: "{autodesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; Tasks: desktopicon; IconFilename: "{app}\app-icon.ico"

[Run]
Filename: "{app}\{#MyAppExeName}"; Description: "Launch {#MyAppName}"; Flags: nowait postinstall skipifsilent shellexec

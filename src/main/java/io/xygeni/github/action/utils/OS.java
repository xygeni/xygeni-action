package io.xygeni.github.action.utils;

import org.apache.commons.lang3.JavaVersion;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;

/**
 * OS - Utility for system properties and OS detection.
 *
 * @author <a href="mailto:lrodriguez@als-es.com">lrodriguez</a>
 * @version 15-abr-2008 $
 */
@SuppressWarnings("unused")
public class OS {
  private static final String JAVA_HOME_KEY = "java.home";
  private static final String USER_HOME_KEY = "user.home";
  private static final String USER_DIR_KEY = "user.dir";
  private static final String JAVA_IO_TMPDIR_KEY = "java.io.tmpdir";

  // OS name, version and arch
  public static final String OS_NAME = getSystemProperty("os.name");
  public static final String OS_VERSION = getSystemProperty("os.version");
  public static final String OS_ARCH = getSystemProperty("os.arch");

  private static final String OS_NAME_WINDOWS_PREFIX = "Windows";

  // OS-checking constants
  public static final boolean IS_OS_WINDOWS = getOSMatchesName(OS_NAME_WINDOWS_PREFIX);
  public static final boolean IS_OS_AIX = getOSMatchesName("AIX");
  public static final boolean IS_OS_HP_UX = getOSMatchesName("HP-UX");
  public static final boolean IS_OS_400 = getOSMatchesName("OS/400");
  public static final boolean IS_OS_IRIX = getOSMatchesName("Irix");
  public static final boolean IS_OS_LINUX = getOSMatchesName("Linux") || getOSMatchesName("LINUX");
  public static final boolean IS_OS_MAC = getOSMatchesName("Mac");
  public static final boolean IS_OS_MAC_OSX = getOSMatchesName("Mac OS X");
  public static final boolean IS_OS_FREE_BSD = getOSMatchesName("FreeBSD");
  public static final boolean IS_OS_OPEN_BSD = getOSMatchesName("OpenBSD");
  public static final boolean IS_OS_NET_BSD = getOSMatchesName("NetBSD");
  public static final boolean IS_OS_SOLARIS = getOSMatchesName("Solaris");
  public static final boolean IS_OS_SUN_OS = getOSMatchesName("SunOS");

  /** True if any unix flavour: AIX, HP-UX, Irix, Linux, macOS, Solaris/SunOS, NetBSD, FreeBSD, OpenBSD or OS/400 */
  public static final boolean IS_OS_UNIX =
    IS_OS_AIX || IS_OS_HP_UX || IS_OS_IRIX || IS_OS_LINUX || IS_OS_MAC_OSX ||
    IS_OS_SOLARIS || IS_OS_SUN_OS || IS_OS_FREE_BSD || IS_OS_OPEN_BSD || IS_OS_NET_BSD;

  /** File encoding, such as {@code Cp1252}. {@code file.encoding} System Property. */
  public static final String FILE_ENCODING = getSystemProperty("file.encoding");

  /** Java file separator ({@code file.separator} System property). Typically / for unix, \ for windows */
  public static final String FILE_SEPARATOR = getSystemProperty("file.separator");

  /** Java path separator ({@code path.separator} System property). Typically : for unix, ; for windows */
  public static final String PATH_SEPARATOR = getSystemProperty("path.separator");

  /** Java installation directory ({@code java.home} System Property). */
  public static final String JAVA_HOME = getSystemProperty(JAVA_HOME_KEY);

  /** Default temp file path ({@code java.io.tmpdir} System Property). */
  public static final String JAVA_IO_TMPDIR = getSystemProperty(JAVA_IO_TMPDIR_KEY);

  /** Java class path ({@code java.class.path} System property) */
  public static final String JAVA_CLASS_PATH = getSystemProperty("java.class.path");

  /** Java class format version number ({@code java.class.version} System property) */
  public static final String JAVA_CLASS_VERSION = getSystemProperty("java.class.version");

  /** Library directories where native libraries will be loaded {{@code java.library.path} System property}. */
  public static final String JAVA_LIBRARY_PATH = getSystemProperty("java.library.path");

  public static final String JAVA_SPECIFICATION_VERSION = getSystemProperty("java.specification.version");
  public static final String JAVA_AWT_HEADLESS = getSystemProperty("java.awt.headless");

  private OS() {}

  /**
   * Get java property or environment property, in that order.
   * Could return null if neither System property nor environment variable exist with that name.
   */
  public static String getProperty(String name) {
    String value = getSystemProperty(name);
    if(value==null) return getEnvironment(name);
    else return value;
  }

  /**
   * Get java property or environment property (in that order), or defVal if not available.
   */
  public static String getProperty(String name, String defVal) {
    String value = getProperty(name);
    return value==null? defVal : value;
  }

  /** Operating system name ({@code os.name} System property) */
  public static String getOsName() { return OS_NAME; }
  /** Operating system architecture ({@code os.arch} System property) */
  public static String getOsArch() { return OS_ARCH; }
  /** Operating system version ({@code os.version} System property) */
  public static String getOsVersion() { return OS_VERSION; }

  public static boolean isWindows() { return IS_OS_WINDOWS; }
  public static boolean isUnix() { return IS_OS_UNIX; }
  public static boolean isMacOS() { return IS_OS_MAC || IS_OS_MAC_OSX; }

  /** Return true if the OS is 64 bits, false otherwise */
  public static boolean is64Bits() {
    if(isWindows()) {
      // http://stackoverflow.com/questions/4748673/how-can-i-check-the-bitness-of-my-os-using-java-j2se-not-os-arch
      String arch = getEnvironment("PROCESSOR_ARCHITECTURE");
      String wow64Arch = getEnvironment("PROCESSOR_ARCHITEW6432");
      if(arch != null) {
        return arch.endsWith("64") || wow64Arch != null && wow64Arch.endsWith("64");
      }

      // ProgramFiles(x86) variable is set on Windows 64 bit systems only
      // http://stackoverflow.com/questions/1856565/how-do-you-determine-32-or-64-bit-architecture-of-windows-using-java
      return getEnvironment("ProgramFiles(x86)") != null;

    } else {
      // For other systems, os.arch is the JVM architecture, not the OS arch.
      // For MAC this works, because apple locks down the JVM to match the OS
      // But for Linux, Solaris... a 32-bit JVM could be installed on a 64-bit system
      return OS_ARCH != null && OS_ARCH.contains("64");
    }
  }

  /** true if java is at least the required version  */
  public static boolean isJavaVersionAtLeast(JavaVersion required) {
    return SystemUtils.isJavaVersionAtLeast(required);
  }

  /** true if java is at most the required version  */
  public static boolean isJavaVersionAtMost(JavaVersion required) {
    return SystemUtils.isJavaVersionAtMost(required);
  }

  /** The home of the java system (jdk/jre), {@code java.home} property. */
  public static File getJavaHome() {
    return new File(System.getProperty(JAVA_HOME_KEY));
  }

  /** The directory in {@code java.io.tmpdir} System property. */
  public static File getTempDir() {
    return new File(System.getProperty(JAVA_IO_TMPDIR_KEY));
  }

  /** True if headless mode enabled, i.e. {@code java.awt.headless} property set to true. */
  public static boolean isJavaAwtHeadless() {
    return Boolean.TRUE.toString().equalsIgnoreCase(JAVA_AWT_HEADLESS);
  }

  /**
   * User home directory ({@code user.home} System property).
   * Like {@code c:\Users\<USERNAME>} on Windows, {@code /home/<USERNAME>} in Linux. {@code /Users/<USERNAME>} in macOS
   */
  public static File getUserHome() { return new File(System.getProperty(USER_HOME_KEY)); }

  /** User directory ({@code user.dir} System property) */
  public static File getUserDir() { return new File(System.getProperty(USER_DIR_KEY)); }

  /** The current directory, an alias for {@link #getUserDir()} */
  public static File getCurrentDir() { return getUserDir(); }

  /**
   * Get the java system variable for name, or null if not found. Same as {@code System.getProperty(property)}.
   */
  public static String getSystemProperty(String property) {
    try {
      return System.getProperty(property);

    } catch (SecurityException ex) {
      // not allowed by security policy
      return null;
    }
  }

  /**
   * Get the environment variable for name, or null if not found. Same as {@code System.getenv(name)}.
   */
  public static String getEnvironment(String name) {
    try {
      return System.getenv(name);
    } catch(Throwable t) {
      /* To avoid nonsense deprecation in JDK1.4.2 and even more stupidity with IBM JVMs, that throw java.lang.Error without any fear... */
      return null;
    }
  }

  /**
   * Add / remove environment variables to this JVM process environment variables.
   * <p/>
   * Use with caution! Affecting the environment may have unintended consequences.
   * The only justified usage is during unit testing, where variables like PATH
   * need to be modified for simulating certain environments.
   * <p/>
   * If
   * <p/>
   * Implementation note: Java does not provide any public mechanism to alter the process environment.
   * This uses a fragile 'evil hack', by reflectively modifying private fields:
   * in the {@code java.lang.ProcessEnvironment} ({@code theEnvironment} or {@code theCaseInsensitiveEnvironment}),
   * or {@code m} field in {@code java.util.Collections.UnmodifiableMap} object return by {@code System.getenv()}.
   * See https://stackoverflow.com/questions/318239/how-do-i-set-environment-variables-from-java.
   *
   * @param addDelVars Map with variables to add. Existing variables are overwritten.
   *                   If the value is null, the variable is removed.
   *
   * @throws ReflectiveOperationException when some reflective operation failed.
   */
  @SuppressWarnings("JavadocLinkAsPlainText")
  public static void modifyEnv(Map<String, String> addDelVars) throws ReflectiveOperationException {
    try {
      Class<?> clazz = Class.forName("java.lang.ProcessEnvironment");

      // case #1: theEnvironment private field (Linux)
      Field theEnvironmentField = clazz.getDeclaredField("theEnvironment");
      theEnvironmentField.setAccessible(true);
      @SuppressWarnings("unchecked")
      Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
      putOrRemove(env, addDelVars);

      // case #2: theCaseInsensitiveEnvironment private field (Windows)
      @SuppressWarnings("JavaReflectionMemberAccess")
      Field theCaseInsensitiveEnvironmentField = clazz.getDeclaredField("theCaseInsensitiveEnvironment");
      theCaseInsensitiveEnvironmentField.setAccessible(true);
      @SuppressWarnings("unchecked")
      Map<String, String> cienv = (Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
      putOrRemove(cienv, addDelVars);

    } catch (NoSuchFieldException e) {
      // case #3: change map returned by
      Class<?>[] classes = Collections.class.getDeclaredClasses();
      Map<String, String> env = System.getenv();

      for(Class<?> cl : classes) {
        if("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
          Field field = cl.getDeclaredField("m");
          field.setAccessible(true);
          Object obj = field.get(env);
          @SuppressWarnings("unchecked") Map<String, String> map = (Map<String, String>) obj;
          putOrRemove(map, addDelVars);
        }
      }
    }
  }

  /**
   * Checks if the JVM process is piped (so {@code System.console() == null}).
   * This may help to check that stdin or stdout are being redirected.
   *
   * Note: Unfortunately, in Java there is no equivalent to Posix' {@code isatty(fd)}.
   */
  public static boolean isPotentiallyPiped() {
    return System.console() == null;
  }

  // Implementation

  private static boolean getOSMatchesName(String osNamePrefix) {
    return isOSNameMatch(OS_NAME, osNamePrefix);
  }

  @SuppressWarnings("SameParameterValue")
  private static boolean isOSNameMatch(String osName, final String osNamePrefix) {
    return osName != null && osName.startsWith(osNamePrefix);
  }

  private static boolean isJavaVersionMatch(final String version, final String versionPrefix) {
    return version != null && version.startsWith(versionPrefix);
  }

  private static void putOrRemove(Map<String, String> map, Map<String, String> newMap) {
    for (Map.Entry<String, String> e : newMap.entrySet()) {
      if(e.getValue()==null) map.remove(e.getKey());
      else map.put(e.getKey(), e.getValue());
    }
  }

}

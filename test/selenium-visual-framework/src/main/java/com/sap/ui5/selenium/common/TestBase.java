package com.sap.ui5.selenium.common;


import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.awt.im.InputContext;
import java.io.File;
import java.net.URL;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import com.sap.ui5.selenium.action.IUserAction;
import com.sap.ui5.selenium.action.UserActionChrome;
import com.sap.ui5.selenium.action.UserActionFirefox;
import com.sap.ui5.selenium.action.UserActionIE;
import com.sap.ui5.selenium.util.Constants;
import com.sap.ui5.selenium.util.Utility;




public class TestBase extends CommonBase{
	
	private static boolean isBeforeAllTests = true;
	
	private final Config config = Config.INSTANCE;
	private final InitService service = InitService.INSTANCE;
	
	public IUserAction userAction;
	
	private String baseUrl = service.getBaseURL();  //"http://veui5infra.dhcp.wdf.sap.corp:8080/uilib-sample";
	
	//Image repository for the current test runtime 
	private final String imagesBasePath = service.getImagesBasePath();
	
	//Image directory for each test class
	private final String testDIR = getTestDIR();
	
	//For expected images
	private final String expectedImagesDIR = testDIR + fileSeparator + "ExpectedImages" + fileSeparator; 
	
	//The images for manual check
	private final String needVerifyImagesDIR = testDIR + fileSeparator + "NeedVerifyImages" + fileSeparator;  
	
	//Temporary Image DIR. It should be empty before testing.
	private final String tempImagesDIR = testDIR + fileSeparator + "TempImages" + fileSeparator;
	
	//differ image DIR. It should be empty before testing
	private final String diffImagesDIR = testDIR + fileSeparator + "DiffImages" + fileSeparator;
	
	//Prefix and Suffix for Image file 
	private final String expectedImagePrefix = "";
	private final String diffImagePrefix = "";
	private final String expectedImageSuffix = "." + Constants.IMAGE_TYPE;
	private final String diffImageSuffix =  "-diff." + Constants.IMAGE_TYPE;
	
	public TestBase(){
		
		initialize();
	}
	
	/** Common initialization for all tests */
	private void initialize(){
		
		//Check if 4 base directories is OK
		createDIR(expectedImagesDIR);
		createDIR(needVerifyImagesDIR);
		createDIR(tempImagesDIR);
		createDIR(diffImagesDIR);
		
		//Clean files before testing
		if (isBeforeAllTests == true) {
			deleteAllFilesInDirectory(new File(tempImagesDIR));
			deleteAllFilesInDirectory(new File(diffImagesDIR));
			deleteAllFilesInDirectory(new File(needVerifyImagesDIR));
			
			isBeforeAllTests = false;
		}
		
		//initial WebDriver
		getDriver();
		
	}
	
	/** Create target directory by full path*/
	private void createDIR(String Path){
		
		File targetDIR = new File(Path);
		
		if(!targetDIR.exists()){
			targetDIR.mkdirs();
		}
	}
	
	/** Delete all files in a directory */
	private boolean deleteAllFilesInDirectory(File directory){
		
		if (!directory.isDirectory()){
			return false;
		}
		
		boolean isSuccess = true;
		File[] files = directory.listFiles();
	
		for (int i = 0; i < files.length; i++){
			
			isSuccess = isSuccess && files[i].delete();
		}
		
		return isSuccess;
	}
	
	/** Get testDIR path with starting "modules" sub-package */
	private String getTestDIR(){
	    
	    String fullName = this.getClass().getName();
        String modulesPackage = ".modules.";
        String testsPackage = ".tests.";	    
        
        if (!fullName.contains(modulesPackage) || !fullName.contains(testsPackage)){
            
            System.out.println("Your package is not correct, please check!");
            System.out.println("Correct package need contain modules and tests package!");
            System.out.println("eg: com.sap.ui5.modules.commons.tests.ButtonTest.java");
            System.exit(1);
        }
        
        fullName = fullName.replace(".tests.", ".");
		int index = fullName.lastIndexOf(modulesPackage) + modulesPackage.length();		
		fullName = fullName.substring(index);
		
		return imagesBasePath + fileSeparator + fullName.replace(".", fileSeparator);
	}

	/** Get browser type, it definition is in Constants.Class */
	public int getBrowserType(){
		return service.getBrowserType();
	}
	
	/** Initial WebDriver and return the instance */
	private WebDriver getDriver(){

		//Get target driver
		switch (getBrowserType()){
		case Constants.FIREFOX:
			if(!initializeFirefoxDriver()){
				if (driver != null){
					driver.quit();
					driver = null;
					System.out.println("Failed to initialize FireFoxDriver!");
				}
			}
			break;

		case Constants.IE:
			if(!initializeIEDriver()){
				if (driver != null){
					driver.quit();
					driver = null;
					System.out.println("Failed to initialize IEDriver!");
				}
			}
			break;
			
		case Constants.CHROME:
			if(!initializeChromeDriver()){
				if (driver != null){
					driver.quit();
					driver = null;
					System.out.println("Failed to initialize ChromeDriver!");
				}
			}
			break;
			
		default:
		case 0:
			System.out.println("Failed to get driver, as the config file is wrong");
			System.exit(1);
		}
		
		return driver;
	}
	
	/** Initial WebDriver common setting */
	private void initializeDriverSetting(WebDriver driver){
		
		driver.manage().timeouts().implicitlyWait(implicitlyWaitTime, TimeUnit.SECONDS);
		driver.manage().timeouts().setScriptTimeout(scriptTimeout, TimeUnit.SECONDS);
		driver.manage().window().maximize();
	}
	
	/** Initialize Fiefox Driver */
	private boolean initializeFirefoxDriver(){
		
		//Initial Remote Firefox Driver
		if (service.isRemoteEnv()) {
			DesiredCapabilities capability = DesiredCapabilities.firefox();
			capability.setVersion(config.getBrowserVersion());
			capability.setJavascriptEnabled(true);
			capability.setPlatform(service.getTargetPlatform());
			
			URL remoteUrl;
			try {
				
				remoteUrl = new URL(service.getRemoteSeleniumServerURL());
				driver = new RemoteWebDriver(remoteUrl, capability);
				initializeDriverSetting(driver);				
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			
			return true;
		} 
		
		
		//Initial Local Firefox Driver
		driver = new FirefoxDriver();
		initializeDriverSetting(driver);
		if (hideFirefoxStatusBar() == false) {
			return false;
		}

		//Initialize UserAction for Firefox
		userAction = new UserActionFirefox();
		userAction.setRtl(isRtlTrue());

		return true;
	}
	
	/** Click the two buttons "Ctrl" and "/" at the same time on the keyboard. 
	 *  To cancel the status bar of browser */
	private boolean hideFirefoxStatusBar(){
		
		Robot robot;
		try {
			robot = new Robot();
			robot.delay(1000);
			Locale keyBoardLocale = InputContext.getInstance().getLocale();
			
			//US KeyBoard
			if (keyBoardLocale.equals(Locale.US)) {
				robot.keyPress(KeyEvent.VK_CONTROL);
				robot.keyPress(KeyEvent.VK_SLASH);
				
				robot.keyRelease(KeyEvent.VK_SLASH);
				robot.keyRelease(KeyEvent.VK_CONTROL);
				
				return true;
			}
			
			//German KeyBorad
			if (keyBoardLocale.equals(Locale.GERMANY)) {
				robot.keyPress(KeyEvent.VK_CONTROL);
				robot.keyPress(KeyEvent.VK_SHIFT);
				robot.keyPress(KeyEvent.VK_7);
				
				robot.keyRelease(KeyEvent.VK_7);
				robot.keyRelease(KeyEvent.VK_SHIFT);
				robot.keyRelease(KeyEvent.VK_CONTROL);
				
				return true;
			}
			
			
			System.out.println("The current keyboard locale (" + keyBoardLocale
			                  + ") is not supported");
			return false;
			
		} catch (AWTException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/** Initialize IE Driver */
	private boolean initializeIEDriver(){
		
		//Initial Remote IE Driver
		if (service.isRemoteEnv()) {
			DesiredCapabilities capability = DesiredCapabilities.internetExplorer();
			capability.setVersion(config.getBrowserVersion());
			capability.setJavascriptEnabled(true);
			capability.setPlatform(service.getTargetPlatform());
			
			URL remoteUrl;
			try {
				
				remoteUrl = new URL(service.getRemoteSeleniumServerURL());
				driver = new RemoteWebDriver(remoteUrl, capability);
				initializeDriverSetting(driver);
				
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			
			return true;
		}
		
		//Initial local IE Driver
		try{
			
			driver = new InternetExplorerDriver();
			initializeDriverSetting(driver);
			
			//Initialize UserAction for IE
			userAction = new UserActionIE();
			userAction.setRtl(isRtlTrue());
			
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}

		return true;
	}
	
	/** Initialize Chrome Driver */
	private boolean initializeChromeDriver(){
		
		//Initial Remote Chrome Driver
		if (service.isRemoteEnv()) {
			DesiredCapabilities capability = DesiredCapabilities.chrome();
			capability.setVersion(config.getBrowserVersion());
			capability.setJavascriptEnabled(true);
			capability.setPlatform(service.getTargetPlatform());
			
			URL remoteUrl;
			try {
				
				remoteUrl = new URL(service.getRemoteSeleniumServerURL());
				driver = new RemoteWebDriver(remoteUrl, capability);
				initializeDriverSetting(driver);
				
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			
			return true;
		}
		
		//Initial local Chrome Driver
		try {
			driver = new ChromeDriver();
			initializeDriverSetting(driver);
			
			//Initialize UserAction for Chrome
			userAction = new UserActionChrome();
			userAction.setRtl(isRtlTrue());
			
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	

	/** API: Get a full with parameters */
	protected String getFullUrl(String targetUrl){
		
		return baseUrl + targetUrl + "?" + "sap-ui-theme=" + config.getUrlParameterTheme() + "&"
				                              + "sap-ui-rtl=" + config.getUrlParameterRtl() + "&"
				                              + "sap-ui-jqueryversion=" + config.getUrlParameterJquery();
	}
	
	/** API: Take a snapShot for a specific element */
	public boolean takeSnapShot(String elementId, String fileName){
		
		Dimension d = userAction.getElementDimension(driver, elementId);
		
		return takeSnapShot(elementId, d.width, d.height, fileName);

	}
	
	/** API: Take a snapshot based on starting a element and specific dimension */
	public boolean takeSnapShot(String elementId, int width, int height, String fileName){
		
		Point location = userAction.getElementLocation(driver, elementId);
		return takeSnapShot(location.x, location.y, width, height, fileName);
	}

	/** Take a screen shot based on specific location and dimension */
	private boolean takeSnapShot(int locationX, int locationY, int width, int height, String fileName, boolean needWrapName){
		
		String filePath = genFullPathForNeedVerifyImage(fileName, needWrapName);
		Point location = new Point(locationX, locationY);
		Dimension dimension = new Dimension(width, height);
		
		if (Utility.takeSnapShot(location, dimension, filePath)){
			
			logTakeScreenShot(filePath);
			return true;
		}
		
		return false;
	}
	
	/** API: Take a screen shot based on specific location and dimension */
	public boolean takeSnapShot(int locationX, int locationY, int width, int height, String fileName){
		
		waitForUI();
		return 	takeSnapShot(locationX, locationY, width, height, fileName, true);
	}

	/** Take a full screen shot */
	private boolean takeScreenShot(String fileName, boolean needWrapName){
		
		String filePath = genFullPathForNeedVerifyImage(fileName, needWrapName);
		
		if (Utility.takeScreenShot(driver, filePath)){
			
			logTakeScreenShot(filePath);
			return true;
		}
		
		return false;
	}
	
	/** API: Take a full screen shot */
	public boolean takeScreenShot(String fileName){
		
		waitForUI();
		return takeScreenShot(fileName, true);
	}
	
	/** Generate full path for image which need be verified */
	private String genFullPathForNeedVerifyImage(String fileName, boolean needWrapName){
		
		if (needWrapName){
			
			String fullPath = needVerifyImagesDIR + expectedImagePrefix + fileName + expectedImageSuffix;
			return fullPath;
			
		}else{
			
			return needVerifyImagesDIR + fileName;
		}
	}
	
	/** Generate full path for expected images */
	private String genFullPathForExpectedImage(String fileName, boolean needWrapName){
		
		if (needWrapName){
			
			String fullPath = expectedImagesDIR + expectedImagePrefix + fileName + expectedImageSuffix;
			return fullPath;
			
		}else{

			return expectedImagesDIR + fileName;
		}
	}
	
	/** Generate full path for diff images */
	private String genFullPathForDiffImage(String fileName, boolean needWrapName){
		
		if (needWrapName){
			
			String fullPath = diffImagesDIR + diffImagePrefix + fileName + diffImageSuffix;
			return fullPath;
			
		}else{
			
			return diffImagesDIR + fileName;
		}
	}

	/** Print log when take a screen shot */
	private void logTakeScreenShot(String fullFilePath){
		System.out.println("A image is created in: " + fullFilePath);
	}
	
	/** Create a file for temp image */
	private File createTempImageFile() throws Exception{
		
		File tempFile;
		tempFile = File.createTempFile("temp", "." + Constants.IMAGE_TYPE, new File(tempImagesDIR));
		return tempFile;
		
	}
	
	/** API: Verify specific element UI by image comparing */
	public void verifyElementUI(String elementId, String expectedImageName){
		
		Point location = userAction.getElementLocation(driver, elementId);
		Dimension dimension = userAction.getElementDimension(driver, elementId);
		
		verifyCustomizedDimension(location.x, location.y, dimension.width, dimension.height, expectedImageName);
	}
	
	/** API: Assert specific element UI by image comparing */
	public void assertElementUI(String elementId, String expectedImageName){
		
		Point location = userAction.getElementLocation(driver, elementId);
		Dimension dimension = userAction.getElementDimension(driver, elementId);
		
		assertCustomizedDimension(location.x, location.y, dimension.width, dimension.height, expectedImageName);
	}
	
	/** API: Verify UI part with customized dimension */
	public void verifyCustomizedDimension(String elementId, int width, int height, String expectedImageName){
	
		Point location = userAction.getElementLocation(driver, elementId);

		verifyCustomizedDimension(location.x, location.y, width, height, expectedImageName);
	}

	/** API: Assert UI part with customized dimension */
	public void assertCustomizedDimension(String elementId, int width, int height, String expectedImageName){
	
		Point location = userAction.getElementLocation(driver, elementId);

		assertCustomizedDimension(location.x, location.y, width, height, expectedImageName);
	}
	
	/** Verify UI part with customized dimension */
	private boolean verifyCustomizedDimension(Point location, Dimension dimension, String expectedImageName){
		
		File expectedImage;
		File actualImage;
		File diffImage;
		try {
			expectedImage = new File(genFullPathForExpectedImage(expectedImageName, true));	
			actualImage = createTempImageFile();
			diffImage = createTempImageFile();
			
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
 		
		
		if (expectedImage.exists()){
			
			if (!Utility.takeSnapShot(location, dimension, actualImage.getPath())){
				
				actualImage.delete();
				diffImage.delete();
				return false;
			}
			
			boolean results = compareImages(expectedImage, actualImage, diffImage);
			
			if (!results) {
				
				//Move actual Image to need verify folder
				Utility.moveFile(actualImage, new File(needVerifyImagesDIR + expectedImage.getName()));
				
				//Move Diff Image to diff folder
				Utility.moveFile(diffImage, new File(genFullPathForDiffImage(expectedImageName, true)));

				System.out.println("The new candidate image is saved on: " + needVerifyImagesDIR + expectedImage.getName());
				System.out.println("Differ Image is created on: " + genFullPathForDiffImage(expectedImageName, true));
			}
			
			return results;
			
		}else{
			
			
			boolean isSuccess = takeSnapShot(location.x, location.y, dimension.width, dimension.height, expectedImage.getName(), false);
			return handleImagesMissed(isSuccess, expectedImage, diffImage);
		}
	}

	/** API: Verify UI part with customized dimension */
	public void verifyCustomizedDimension (int locationX, int locationY, int width, int height, String expectedImageName) {
		
		waitForUI();
		verifyTrue(verifyCustomizedDimension (new Point(locationX, locationY), new Dimension(width, height),  expectedImageName));
	}
	
	/** API: Assert UI part with customized dimension */
	public void assertCustomizedDimension (int locationX, int locationY, int width, int height, String expectedImageName) {
		
		waitForUI();
		Assert.assertTrue("Customized Dimension UI is matched? ", verifyCustomizedDimension(new Point(locationX, locationY), new Dimension(width, height),  expectedImageName));
	}
	
	/** API: Verify UI of browser view box */
	public void verifyBrowserViewBox(String expectedImageName){
	    
	    Point viewBoxLocation = userAction.getBrowserViewBoxLocation(driver);
	    Dimension viewBoxDimension = userAction.getBrowserViewBoxDimension(driver);
	    
	    verifyCustomizedDimension(viewBoxLocation.x, viewBoxLocation.y, 
	                              viewBoxDimension.width, viewBoxDimension.height, 
	                              expectedImageName); 
	}
	
	/** API: Assert UI of browser view box */
	public void assertBrowserViewBox(String expectedImageName){
	    
	    Point viewBoxLocation = userAction.getBrowserViewBoxLocation(driver);
	    Dimension viewBoxDimension = userAction.getBrowserViewBoxDimension(driver);
	    
	    assertCustomizedDimension(viewBoxLocation.x, viewBoxLocation.y, 
	                              viewBoxDimension.width, viewBoxDimension.height, 
	                              expectedImageName); 
	}


	/** Verify full page UI by image comparing */
 	private boolean verifyFullPage(String expectedImageName){
 		
		File expectedImage;
		File actualImage;
		File diffImage;
		try {
			expectedImage = new File(genFullPathForExpectedImage(expectedImageName, true));	
			actualImage = createTempImageFile();
			diffImage = createTempImageFile();
			
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
 		
		if (expectedImage.exists()){
			
			if (!Utility.takeScreenShot(driver, actualImage.getPath())){
				
				actualImage.delete();
				diffImage.delete();
				return false;
			}
			
			boolean results = compareImages(expectedImage, actualImage, diffImage);
			
			if (!results) {
				
				//Move actual Image to need verify folder
				Utility.moveFile(actualImage, new File(needVerifyImagesDIR + expectedImage.getName()));
				
				//Move Diff Image to diff folder
				Utility.moveFile(diffImage, new File(genFullPathForDiffImage(expectedImageName, true)));
				
				System.out.println("The new candidate image is saved on: " + needVerifyImagesDIR + expectedImage.getName());
				System.out.println("Differ Image is created on: " + genFullPathForDiffImage(expectedImageName, true));				
			}
			
			return results;

			
		}else{
			
			boolean isSuccess = takeScreenShot(expectedImage.getName(), false);
			return handleImagesMissed(isSuccess, expectedImage, diffImage);
		}
 		
	}
	
	/** API: Verify full page UI by image comparing */
	public void verifyFullPageUI(String expectedImageName){
		
		waitForUI();
		verifyTrue(verifyFullPage(expectedImageName));
	}
	
	/** API: Assert full page UI by image comparing */
	public void assertFullPageUI(String expectedImageName){
		
		waitForUI();
		Assert.assertTrue("Full Page UI is matched?: ", verifyFullPage(expectedImageName));
	}
	
	/** Handler when expected images are missed */
	private boolean handleImagesMissed(boolean takeSnapshotSuccess, File expectedImage, File fileDiff){
		
		if (!takeSnapshotSuccess){
			
			System.out.println("Failed to create a image at path: " + needVerifyImagesDIR + expectedImage.getName());
		}
		
		fileDiff.delete();
		return service.isDevMode();
	}
	
	/** Compare images and set parameter: pixelThreshold and colorDistance  */
	private boolean compareImages(File expectedImage, File actualImage, File diffImage, StringBuilder resultMessage) throws Exception{
		
		int pixelThreshold = 0;
		int colorDistance = 0;
		
		return Utility.imageComparer(expectedImage, actualImage, diffImage, colorDistance, pixelThreshold, resultMessage);
		
	}

	/** Compare Images files */
	private boolean compareImages(File expectedImage, File actualImage, File diffImage){
		
		StringBuilder resultMessage = new StringBuilder();
		
		boolean isMatched;
		try {

			System.out.println();
			System.out.print("####  " + "Comparing: " + expectedImage.getName() + 
					         " (" + getClass().getSimpleName() + "." + testName.getMethodName() + ")");
			isMatched = compareImages(expectedImage, actualImage, diffImage, resultMessage);
		} catch (Exception e) {
			
			System.out.println("    ==> FAILED");
			System.out.println("Errors occur during image comparing, Stop this image comparing! ");
			System.out.println("#####################");
			e.printStackTrace();
			System.out.println("#####################");
			
			Utility.deleteFile(actualImage);
			Utility.deleteFile(diffImage);
			return false;
		}

		if (isMatched){
			
			System.out.println("    ==> PASS");
			Utility.deleteFile(actualImage);
			Utility.deleteFile(diffImage);
			
		} else {
			System.out.println("    ==> FAILED");
			System.out.println(resultMessage.toString());
		}
		
		return isMatched;
		
	}
	
	/** Return true if "sap-ui-rtl" is true */
	public boolean isRtlTrue(){
		
		if (Boolean.parseBoolean(config.getUrlParameterRtl()) == true){
			return true;
		}else{
			return false;
		}
	}
	
	/** Show Tooltip for all browser by wrapping userAction.mouseOver() */
	public void showToolTip(String elementId, int waitTimeMillsecond) {
		
		if (getBrowserType() == Constants.FIREFOX) {
			userAction.mouseOver(driver, elementId, waitTimeMillsecond);
			userAction.mouseMoveToStartPoint(driver); 
		}
		
		userAction.mouseOver(driver, elementId, waitTimeMillsecond);
	}
	
	
}

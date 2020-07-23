/*!
 * ${copyright}
 */
sap.ui.define([
	"sap/ui/test/opaQunit",
	"sap/ui/test/Opa5"
], function (opaTest, Opa5) {
	"use strict";

	//*****************************************************************************
	opaTest("Check if messages for items that are not currently seen are loaded",
		function (Given, When, Then) {
			When.onMainPage.showSalesOrder("103");
			Then.onMainPage.checkSalesOrderLoaded("103");
			Then.onMainPage.checkSalesOrderItemsLoaded("103");
			When.onMainPage.rememberCurrentMessageCount();

			When.onMainPage.toggleMessagePopover();
			Then.onMainPage.checkMessagePopoverOpen();
			Then.onMainPage.checkMessageInPopover("050", "order");
			When.onMainPage.toggleMessagePopover();

			When.onMainPage.pressFixAllQuantities();
			Then.onMainPage.checkMessageCountHasChangedByX(-1);

			When.onMainPage.toggleMessagePopover();
			Then.onMainPage.checkMessagePopoverOpen();
			Then.onMainPage.checkMessageNotInPopover("050", "order");
			When.onMainPage.toggleMessagePopover();

			When.onMainPage.scrollTable(1);
			Then.onMainPage.checkItemQuantities();

			When.onMainPage.scrollToTop();
		});
});
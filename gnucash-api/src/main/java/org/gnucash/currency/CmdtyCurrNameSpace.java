package org.gnucash.currency;

public class CmdtyCurrNameSpace {

    // Currency: Note that the old "ISO4217" has been deprecated
    public static final String CURRENCY = CmdtyCurrID.Type.CURRENCY.toString();
    
    // Semi-formal, non-standardized but widely-used abbreviations 
    // of major exchanges
    public enum Exchange {
	
	// ------------------------
	// Americas
	// Cf. https://en.wikipedia.org/wiki/List_of_stock_exchanges_in_the_Americas
	// ------------------------
	
	// North America
	CSE, 	// Canadian Securities Exchange 
	MX, 	// Montreal Exchange 
	NASDAQ_CANADA, 	// NASDAQ Canada 
	TSX, 	// Toronto Stock Exchange 
	 	// TSX Venture Exchange 
	NEO, 	// NEO Exchange
	BMV, 	// Bolsa Mexicana de Valores 
	BIVA, 	// BIVA - Bolsa Institucional de Valores 
	BATS, 	// BATS Global Markets 
	BZX,	// BZX Exchange 
	BYX,	// BYX Exchange 
	BOX, 	// Boston Options Exchange 
	C2, 	// C2 Options Exchange 
	CBOE, 	// Chicago Board Options Exchange 
	 	// CBOE Stock Exchange- Ceased trading April 30, 2014[2] 
	CHX, 	// Chicago Stock Exchange 
	EDGA,	// EDGA
	EDGX, 	// EDGX
	IEX, 	// IEX 
	ISE, 	// ISE and ISE Gemini 
	LTSE, 	// Long Term Stock Exchange 
	MEMX, 	// Members Exchange (MEMX) 
	MIAX, 	// Miami International Securities Exchange 
	NASDAQ, // NASDAQ 
	BX, 	// NASDAQ OMX BX 
	PHLX, 	// NASDAQ OMX PHLX 
	NYSE, 	// New York Stock Exchange 
	NYSE_ARCA, 	// NYSE Arca 
	NYSE_AMERICAN,	// NYSE American (formerly NYSE MKT and 
			// American Stock Exchange (AMEX)
	
	// Central America
	BNV, 	// Bolsa Nacional de Valores 
	BVES, 	// Bolsa de Valores de El Salvador 
	BVNSA, 	// Bolsa Nacional de Valores 
	BCV, 	// Bolsa Centroamericana de Valores 
	BHV, 	// Bolsa Honduras de Valores 
	BVDN, 	// Bolsa de Valores de Nicaragua 
	BVP,	// Bolsa de Valores de Panamá 
	
	// South America
	BCBA, 	// Buenos Aires Stock Exchange 
	MAE, 	// Mercado Abierto Electrónico 
	BCR, 	// Rosario Stock Exchange 
	// BVB,    // Archived 2017-09-16 at the Wayback Machine 	Bolsa Boliviana de Valores 
	B3, 	// B3 
	BVRJ, 	// Rio de Janeiro Stock Exchange 
	BM_AND_F, 	// Brazilian Mercantile and Futures Exchange 
	BCMM, 	// Bolsa de Cereais e Mercardorias de Maringá 
	BOVMESB, 	// Bolsa de Valores Minas - Espírito Santo - Brasília
	// Conflict with Shanghai Stock Exchange
	// SSE, 	// Santiago Stock Exchange (MILA) 
	BEC, 	// Bolsa Electronica de Chile 
	BOVALPO, 	// Valparaíso Stock Exchange 
	BVC, 	// Colombia Stock Exchange (MILA) 
	BVG, 	// Bolsa de Valores de Guayaquil 
	BVQ, 	// Bolsa de Valores de Quito 
	GASCI, 	// Guyana Stock Exchange 
	BVA, 	// Bolsa de Valores de Asunción 
	BVL, 	// Lima Stock Exchange (MILA)
	// Conflict with Shanghai Stock Exchange
	// SSE, 	// Suriname Stock Exchange 
	BVM, 	// Bolsa de Valores de Montevideo 
	BEVSA, 	// Bolsa Electronica de Valores de Uruguay
	// Conflict with Colombia Stock Exchange (MILA)
	// BVC,	// Bolsa de Valores de Caracas 
	
	// Caribbean
	// ...

	
	// ------------------------
	// Europe
	// Cf. https://en.wikipedia.org/wiki/List_of_European_stock_exchanges
	// ------------------------
	
	EURONEXT, // Euronext; 
		  // Caution: Do not confuse with EUREX, which trades derivatives only
	LSEG,	// London Stock Exchange
	XETRA,  // XETRA / Frankfurt Stock Exchange / Deutsche Boerse
	SIX,	// Swiss Exchange
	NASDAQ_NORDIC, // Nasdaq Nordic
	
	// ...and a couple of minor ones.
	
	// ------------------------
	// Asia
	// Cf. https://en.wikipedia.org/wiki/List_of_Asian_stock_exchanges
	// ------------------------
	
	// Central Asia
	KASE, 	// Kazakhstan Stock Exchange 
	AIX, 	// Astana International Exchange 
	KSE, 	// Kyrgyz Stock Exchange (KSE) 
	BTS, 	// Stock Exchange of Kyrgyzstan (BTS) 
	CASE, 	// Central Asian Stock Exchange 
	SRCMET, // State Commodity and Raw Material Exchange of Turkmenistan 
	UZSE, 	// Tashkent Stock Exchange 
	
	// Eastern Asia
	BSE, 	// Beijing Stock Exchange 
	DCE, 	// Dalian Commodity Exchange 
	CFFEX, 	// China Financial Futures Exchange 
	SHFE, 	// Shanghai Futures Exchange 
	SHME, 	// Shanghai Metal Exchange 
	SSE, 	// Shanghai Stock Exchange 
	SZSE, 	// Shenzhen Stock Exchange 
	ZCE, 	// Zhengzhou Commodity Exchange 
	SEHK, 	// Hong Kong Stock Exchange 
	HKEX, 	// Hong Kong Exchanges and Clearing 
	JPX, 	// Tokyo Stock Exchange[4] 
	 	// JASDAQ 
		// JASDAQ NEO 
		// Mothers 
		// Tokyo Pro Market (formerly Tokyo AIM) 
		// Osaka Exchange (formerly Osaka Securities Exchange) 
	// NSE, 	// Nagoya Stock Exchange 
	Centrex, // Centrex 
	FSE, 	// Fukuoka Stock Exchange 
	QB, 	// Q-Board 
	// Caution: conflict with Shanghai Stock Exchange
	// SSE, 	// Sapporo Securities Exchange
	AMB, 	// Ambitious 
	MOX, 	// Macao Financial Asset Exchange 
	MSE, 	// Mongolian Stock Exchange 
		// Ulaanbaatar Securities Exchange 
	MCE, 	// Mongolian Commodity Exchange 
		// Chosun Stock Exchange[9][10] (formerly Chosun Exchange (1932)) 
	KRX, 	// Korea Exchange[12] 
		// Korea Stock Exchange[13][14][9] 
	KOSDAQ, // KOSDAQ 
	TWSE, 	// Taiwan Stock Exchange 
	TPEX, 	// Taipei Exchange 
	TAIFEX,	// Taiwan Futures Exchange 
	
	// Northern Asia
	MOEX,   // Moscow Exchange
	
	// Southeast Asia
	CSX, 	// Cambodia Securities Exchange 
	IDX, 	// Indonesia Stock Exchange 
	JFX, 	// Jakarta Futures Exchange 
	ICDX, 	// Indonesia Commodity and Derivatives Exchange 
	LSX, 	// Lao Securities Exchange 
	MYX, 	// Bursa Malaysia 
		// Malaysia Derivatives Exchange 
		// MESDAQ 
	FSC, 	// FUSANG Exchange 
	MSEC, 	// Myanmar Securities Exchange Centre 
	YSX, 	// Yangon Stock Exchange 
	PDEX, 	// Philippine Dealing Exchange 
	PSE, 	// Philippine Stock Exchange 
	MCX, 	// Manila Commodity Exchange 
	SGX, 	// Singapore Exchange 
	SET, 	// Stock Exchange of Thailand 
	MAI, 	// Market for Alternative Investment 
	BEX, 	// Bond Electronic Exchange 
	AFEX, 	// AFET 
	TFEX, 	// Thailand Futures Exchange 
	VNX, 	// Vietnam Stock Exchange 
	HSX, 	// Ho Chi Minh Stock Exchange 
	HNX,	// Hanoi Stock Exchange 
	
	// Southern Asia
	AFX, 	// Afghanistan Stock Exchange
	// Conflict with Canadian Securities Exchange
	// CSE, 	// Chittagong Stock Exchange 
	DSE, 	// Dhaka Stock Exchange 
	RSEBL, 	// Royal Securities Exchange of Bhutan
	// Conflict with Beijing Stock Exchange
	// BSE, 	// Bombay Stock Exchange 
//	CSE, 	// Calcutta Stock Exchange 
	INX, 	// India International Exchange 
	ICEX, 	// Indian Commodity Exchange 
//	MCX, 	// Multi Commodity Exchange 
	NCDEX, 	// National Commodity and Derivatives Exchange 
	NSE, 	// National Stock Exchange of India 
	TSE, 	// Tehran Stock Exchange 
	IFB, 	// Iran Fara Bourse 
	IME, 	// Iran Mercantile Exchange 
	IRENEX, 	// Iranian Energy Exchange 
//	MSE, 	// Maldives Stock Exchange 
	NEPSE, 	// Nepal Stock Exchange 
	PSX, 	// Pakistan Stock Exchange 
//	CSE, 	// Colombo Stock Exchange 
	
	// Western Asia
//	BSE, 	// Bahrain Stock Exchange 
//	ISX, 	// Iraq Stock Exchange 
	TASE, 	// Tel Aviv Stock Exchange 
//	ASE, 	// Amman Stock Exchange 
	BK, 	// Boursa Kuwait 
//	BSE, 	// Beirut Stock Exchange 
//	MSM, 	// Muscat Securities Market 
//	PSE, 	// Palestine Securities Exchange 
	DSM, 	// Doha Securities Market 
	Tadawul, 	// Tadawul 
//	DSE, 	// Damascus Securities Exchange 
	ADSM, 	// Abu Dhabi Securities Market 
	DFM, 	// Dubai Financial Market 
	NASDAQ_DUBAI, // Dubai 	NASDAQ Dubai 
	DGCX, 	// Dubai Gold & Commodities Exchange 
	
	// ------------------------
	// Oceania
	// Cf. https://en.wikipedia.org/wiki/List_of_stock_exchanges_in_Oceania
	// ------------------------
	
	APTEX, 	// Sydney 
	APXL, 	// Melbourne 
	ASX, 	// Sydney 
	CXA, 	// Melbourne 
	NSX, 	// Sydney 
	SPX, 	// Suva 
	NZX, 	// Wellington 
	PNGX, 	// Port Moresby 

	// ------------------------

	// ::TODO: some more perhaps...
	
	// ------------------------
	
	UNSET
    }

    // Formal Market Identified Codes (MIC); standardized in ISO 10383
    // Cf.:
    //  - https://en.wikipedia.org/wiki/List_of_stock_exchanges
    //  - https://en.wikipedia.org/wiki/Market_Identifier_Code 
    public enum MIC {
	
	// 1) Major Stock Exchanges
	XNYS, // New York Stock Exchange 
	XNAS, // Nasdaq
	XSHG, // Shanghai Stock Exchange 
	XAMS, // Euronext
	XBRU, // Euronext
	XMSM, // Euronext 
	XLIS, // Euronext
	XMIL, // Euronext
	XOSL, // Euronext
	XPAR, // Euronext
	XJPX, // Japan Exchange Group 
	XSHE, // Shenzhen Stock Exchange 
	XHKG, // Hong Kong Stock Exchange 
	XBOM, // Bombay Stock Exchange 
	XNSE, // National Stock Exchange (India)
	XTSE, // Toronto Stock Exchange 
	XLON, // London Stock Exchange 
	XSAU, // Saudi Stock Exchange (Tadawul) 
	XKOS, // Korea Exchange 
	XFRA, // Deutsche Boerse
	XSWX, // SIX Swiss Exchange
	XTAI, // Taiwan Stock Exchange 
	XASX, // Australian Securities Exchange 
	XJSE, // Johannesburg Stock Exchange 
	
	// ... and many others
	
	UNSET
    }
    
    // ::TODO
    // Cf. https://en.wikipedia.org/wiki/List_of_stock_market_indices
//    enum Index {
//
//    }

    // ::TODO
//    public Other {
//	FUND
//	UNSET
//    }

}

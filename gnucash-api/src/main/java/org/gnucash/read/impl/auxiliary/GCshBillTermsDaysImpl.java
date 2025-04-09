package org.gnucash.read.impl.auxiliary;

import org.gnucash.generated.GncV2;
import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.auxiliary.GCshBillTermsDays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GCshBillTermsDaysImpl implements GCshBillTermsDays {

	private static final Logger LOGGER = LoggerFactory.getLogger(GCshBillTermsDaysImpl.class);

	/**
	 * the JWSDP-object we are facading.
	 */
	private final GncV2.GncBook.GncGncBillTerm.BilltermDays jwsdpPeer;

	// ---------------------------------------------------------------

	/**
	 * @param peer the JWSDP-object we are facading.
	 * @see #jwsdpPeer
	 */
	@SuppressWarnings("exports")
	public GCshBillTermsDaysImpl(final GncV2.GncBook.GncGncBillTerm.BilltermDays peer) {
		super();
		jwsdpPeer = peer;
	}

	// ---------------------------------------------------------------

	@Override
	public Integer getDueDays() {
		return jwsdpPeer.getBtDaysDueDays();
	}

	@Override
	public Integer getDiscountDays() {
		return jwsdpPeer.getBtDaysDiscDays();
	}

	@Override
	public FixedPointNumber getDiscount() {
		if (jwsdpPeer.getBtDaysDiscount() == null) {
			return null;
		}
		return new FixedPointNumber(jwsdpPeer.getBtDaysDiscount());
	}

	// ---------------------------------------------------------------

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("[GCshBillTermsDaysImpl:");

		buffer.append(" due-days: ");
		buffer.append(getDueDays());

		buffer.append(" discount-days: ");
		buffer.append(getDiscountDays());

		buffer.append(" discount: ");
		buffer.append(getDiscount());

		buffer.append("]");

		return buffer.toString();
	}

}

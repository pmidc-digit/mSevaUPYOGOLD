/*
 * eGov suite of products aim to improve the internal efficiency,transparency,
 *    accountability and the service delivery of the government  organizations.
 *
 *     Copyright (C) <2015>  eGovernments Foundation
 *
 *     The updated version of eGov suite of products as by eGovernments Foundation
 *     is available at http://www.egovernments.org
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see http://www.gnu.org/licenses/ or
 *     http://www.gnu.org/licenses/gpl.html .
 *
 *     In addition to the terms of the GPL license to be adhered to in using this
 *     program, the following additional terms are to be complied with:
 *
 *         1) All versions of this program, verbatim or modified must carry this
 *            Legal Notice.
 *
 *         2) Any misrepresentation of the origin of the material is prohibited. It
 *            is required that all modified versions of this material be marked in
 *            reasonable ways as different from the original version.
 *
 *         3) This license does not grant any rights to any user of the program
 *            with regards to rights under trademark law for use of the trade names
 *            or trademarks of eGovernments Foundation.
 *
 *   In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 */
package org.egov.lcms.transactions.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.egov.infra.persistence.entity.AbstractAuditable;
import org.egov.infra.persistence.validator.annotation.CompareDates;
import org.egov.infra.persistence.validator.annotation.DateFormat;
import org.egov.infra.persistence.validator.annotation.Required;
import org.egov.infra.persistence.validator.annotation.ValidateDate;
import org.egov.infra.utils.DateUtils;
import org.egov.infra.validation.exception.ValidationError;
import org.egov.lcms.utils.constants.LcmsConstants;
import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = "EGLC_LEGALCASEDISPOSAL")
@SequenceGenerator(name = LegalcaseDisposal.SEQ_EGLC_LEGALCASEDISPOSAL, sequenceName = LegalcaseDisposal.SEQ_EGLC_LEGALCASEDISPOSAL, allocationSize = 1)
@CompareDates(fromDate = "consignmentDate", toDate = LcmsConstants.DISPOSAL_DATE, dateFormat = "dd/MM/yyyy", message = "consignmentDate.greaterThan.disposalDate")
public class LegalcaseDisposal extends AbstractAuditable {
    private static final long serialVersionUID = 1517694643078084884L;
    public static final String SEQ_EGLC_LEGALCASEDISPOSAL = "SEQ_EGLC_LEGALCASEDISPOSAL";

    @Id
    @GeneratedValue(generator = SEQ_EGLC_LEGALCASEDISPOSAL, strategy = GenerationType.SEQUENCE)
    private Long id;
    @ManyToOne
    @NotNull
    @Valid
    @JoinColumn(name = "legalcase", nullable = false)
    private Legalcase legalcase;
    @Required(message = "disposalDate.null")
    @DateFormat(message = "invalid.fieldvalue.model.disposalDate")
    @ValidateDate(allowPast = true, dateFormat = LcmsConstants.DATE_FORMAT, message = "disposalDate.lessthan.currentDate")
    private Date disposalDate;
    @Length(max = 1024, message = "io.disposalDetails.length")
    private String disposalDetails;
    @DateFormat(message = "invalid.fieldvalue.model.consignmentDate")
    private Date consignmentDate;

    public Date getDisposalDate() {
        return disposalDate;
    }

    public void setDisposalDate(final Date disposalDate) {
        this.disposalDate = disposalDate;
    }

    public String getDisposalDetails() {
        return disposalDetails;
    }

    public void setDisposalDetails(final String disposalDetails) {
        this.disposalDetails = disposalDetails;
    }

    public Date getConsignmentDate() {
        return consignmentDate;
    }

    public void setConsignmentDate(final Date consignmentDate) {
        this.consignmentDate = consignmentDate;
    }

    public Legalcase getLegalcase() {
        return legalcase;
    }

    public void setLegalcase(final Legalcase legalcase) {
        this.legalcase = legalcase;
    }

    public List<ValidationError> validate() {
        final List<ValidationError> errors = new ArrayList<ValidationError>();

        if (!DateUtils.compareDates(getDisposalDate(), getLegalcase().getCasedate()))
            errors.add(new ValidationError(LcmsConstants.DISPOSAL_DATE, "disposalDate.greaterthan.caseDate"));

        for (final Hearings hearingsObj : legalcase.getHearings()) {
            int i = 0;
            if (!DateUtils.compareDates(getDisposalDate(), hearingsObj.getHearingDate())) {
                errors.add(new ValidationError(LcmsConstants.DISPOSAL_DATE, "disposalDate.greaterthan.hearingDate"));
                i++;
            }
            if (i > 0)
                break;
        }

        for (final Judgment judgmentObj : getLegalcase().getEglcJudgments()) {
            if (!DateUtils.compareDates(getDisposalDate(), judgmentObj.getOrderDate()))
                errors.add(new ValidationError(LcmsConstants.DISPOSAL_DATE, "disposalDate.greaterthan.judgementDate"));
            for (final Judgmentimpl judgementImpl : judgmentObj.getEglcJudgmentimpls())
                if (!DateUtils.compareDates(getDisposalDate(), judgementImpl.getDateofcompliance()))
                    errors.add(new ValidationError(LcmsConstants.DISPOSAL_DATE,
                            "disposalDate.greaterthan.judgementImplDate"));
        }

        return errors;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

}

import React from 'react';
import Select from '@atlaskit/select';
import { DatePicker } from '@atlaskit/datetime-picker';
import Button, { ButtonGroup } from '@atlaskit/button';
import { typography } from '@atlaskit/theme';
import styled from 'styled-components';
import TextField from '@atlaskit/textfield';
import Form, {
    Field,
    FormHeader,
    FormFooter,
} from '@atlaskit/form';

const Heading = styled.div`
  ${props => props.mixin};
`;

const helpDeskLink = "https://sprintnews.atlassian.net/servicedesk/customer/portal/2";
const InvalidLicense = -2;

export default (props) => (
    <Form onSubmit={data => props.onSubmit(data)}>
        {({ formProps }) => (
            <form {...formProps}>
                <FormHeader title="Setup your sprint report" />
                <Field name="selectedSprint" defaultValue={props.selectedSprint} label="Sprint" isRequired>
                    {({ fieldProps }) =>
                        <Select {...fieldProps}
                                options={props.sprints.map((sprint) => {
                                        return { label: sprint.name, value: sprint.id };
                                    }
                                )}
                        />
                    }
                </Field>
                <div style={{ display: 'flex' }}>
                    <Field name="teamName" defaultValue={props.teamName} label="Team name" isRequired>
                        {({ fieldProps }) => <TextField {...fieldProps} maxLength={30} style={{ width: '220px' }} />}
                    </Field>
                    &nbsp;&nbsp;
                    <Field name="teamCity" defaultValue={props.teamCity} label="Team city" isRequired>
                        {({ fieldProps }) => <TextField {...fieldProps} maxLength={40} style={{ width: '270px' }} />}
                    </Field>
                </div>
                <div style={{ display: 'flex' }}>
                    <Field name="productOwner" defaultValue={props.productOwner} label="Product Owner" isRequired>
                        {({ fieldProps }) => <TextField {...fieldProps} maxLength={30} style={{ width: '220px' }} />}
                    </Field>
                </div>
                <div id="sprint-dates-div" style={{ display: 'grid', gridTemplateColumns: '200px 10px 200px' }}>
                    <Field name="startDate" defaultValue={props.startDate} label="Sprint start">
                        {({ fieldProps }) => <DatePicker {...fieldProps} />}
                    </Field>
                    <span />
                    <Field name="endDate" defaultValue={props.endDate} label="Sprint end">
                        {({ fieldProps }) => <DatePicker {...fieldProps} />}
                    </Field>
                </div>
                <div id="sprint-demo-div" style={{ display: 'grid', gridTemplateColumns: '200px' }}>
                    <Field name="reviewDate" defaultValue={props.reviewDate} label="Sprint Review (demo)">
                        {({ fieldProps }) => <DatePicker {...fieldProps} />}
                    </Field>
                </div>

                <FormFooter>
                    <div id="sprint-demo-div" style={{ display: 'grid', gridTemplateColumns: 'auto 190px', width: '100%' }}>
                        <Heading id="window-title" mixin={typography.h100()} style={{ display: 'grid', marginTop: '0px' }}>
                            <p style={{ margin: '0px' }}>Suggestions or bugs?</p>
                            <p style={{ margin: '0px' }}>Please click&nbsp;<a href={helpDeskLink} target="_blank" rel="noopener noreferrer">here</a></p>
                        </Heading>
                        <ButtonGroup>
                            <Button type="submit" appearance="primary" isLoading={props.isProcessing} isDisabled={props.errorCode === InvalidLicense}>
                                Generate
                            </Button>
                            <Button appearance="subtle" onClick={props.onCancelClick} isDisabled={props.isProcessing}>Cancel</Button>
                        </ButtonGroup>
                    </div>
                </FormFooter>

            </form>
        )}
    </Form>
);

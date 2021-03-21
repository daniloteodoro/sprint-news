import WarningIcon from "@atlaskit/icon/glyph/warning";
import Banner from "@atlaskit/banner";
import React, { Fragment } from "react";

export default (props) => (
    <Banner
        icon={<WarningIcon label="Warning icon" secondaryColor="inherit" />}
        isOpen={props.errorCode && props.errorCode === 409}
    >
        <Fragment>
            Stories don't follow the User Story template. See the <a href="https://sprint-news.com/" target="_blank" rel="noopener noreferrer">FAQ</a>.
        </Fragment>
    </Banner>
);

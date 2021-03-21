import WarningIcon from "@atlaskit/icon/glyph/warning";
import Banner from "@atlaskit/banner";
import React from "react";

export default (props) => (
    <Banner
        icon={<WarningIcon label="Warning icon" secondaryColor="inherit" />}
        isOpen={props.errorCode && props.errorCode !== 409}
    >
        {props.message}
    </Banner>
);

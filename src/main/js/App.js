import React from 'react';
import ReactDOM from 'react-dom';
import InputForNewspaper from './components/newspaper/InputForNewspaper';
import generateReport, { downloadReport, getMetaContentByName } from './report/reporting';
import WarningBanner from './components/WarningBanner';
import InvalidUserStoriesBanner from './components/InvalidUserStoriesBanner';

const InvalidBoard = -1;
// const InvalidLicense = -2;

class App extends React.Component {

	constructor(props) {
		super(props);
		this.state = {
			teamName: undefined,
			teamCity: undefined,
			selectedSprint: undefined,
			productOwner: undefined,
			sprints: [],
			startDate: undefined,
			endDate: undefined,
			reviewDate: undefined,

			isProcessing: false,
			errorCode: undefined,
			errorMessage: '',
		};
		this.handleFormSubmitted = this.handleFormSubmitted.bind(this);
		this.handleCancelClick = this.handleCancelClick.bind(this);
	}

	findAllSprintsFor(boardId) {
		AP.request(`/rest/agile/1.0/board/${boardId}/sprint`)
			.then(data => {
				const sprintsFound = JSON.parse(data.body).values || [];
				this.setState({sprints: sprintsFound}, () => {
					if (this.state.sprints.length > 0) {
						const firstSprint = this.state.sprints[0];
						this.setState({selectedSprint: { label: firstSprint.name, value: firstSprint.id }});
					}
				});
			})
			.catch(e => {
				this.setState({sprints: []});
				console.error(`Error getting sprints for board ${boardId}`, e.err);
			});
	}

	componentDidMount() {
		const urlParams = new URLSearchParams(window.location.search);

		if (!urlParams.has('board-id') || !urlParams.get('board-id')) {
			console.warn("Param 'board-id' was not found");
			this.setState({ isProcessing: false, errorCode: InvalidBoard, errorMessage: "Could not access board information, try again later." });
			return;
		}

		/* Free version!
			if (!urlParams.has('lic') || urlParams.get('lic') !== 'active') {
				console.warn("Invalid licensing");
				this.setState({ isProcessing: false, errorCode: InvalidLicense, errorMessage: "Your license is not active, please check with your Jira Admin." });
				return;
			}
		 */

		const selectedBoardId = urlParams.get('board-id');
		this.findAllSprintsFor(selectedBoardId);
	};

	handleFormSubmitted(data) {
		this.setState({isProcessing: true, errorCode: undefined, errorMessage: undefined});
		const sprintInformation = Object.assign({}, data, {sprints: []});
		const token = getMetaContentByName("token");
		generateReport(sprintInformation, token)
			.then(data => {
				if (!(data instanceof ArrayBuffer)) {
					// Handle error
					if (data.status === 401 || data.status === 403 || data.status === 405) {
						console.warn(data.status)
						AP.dialog.close({});
						return;
					}
					let msg = "There was an error contacting the server. Please try again later.";
					if (data.status === 400 || data.status === 409 || data.status === 422) {
						if (data.message)
							msg = data.message;
					}
					this.setState({ isProcessing: false, errorCode: data.status, errorMessage: msg });
				} else {
					downloadReport('newspaper.pdf', "application/pdf", data);
					this.setState({ isProcessing: false, errorCode: undefined, errorMessage: undefined });
					AP.dialog.close({});
				}
			});
	}

	handleCancelClick() {
		AP.dialog.close({});
	}

	render() {
		return (
			<div style={{backgroundColor: 'white', padding: '20px', paddingTop: '10px', }}>
				<WarningBanner errorCode={this.state.errorCode} message={this.state.errorMessage} />
				<InvalidUserStoriesBanner errorCode={this.state.errorCode} message={this.state.errorMessage} />
				<InputForNewspaper
					teamName={this.state.teamName}
					teamCity={this.state.teamCity}
					productOwner={this.state.productOwner}
					startDate={this.state.startDate}
					endDate={this.state.endDate}
					reviewDate={this.state.reviewDate}
					selectedSprint={this.state.selectedSprint}
					sprints={this.state.sprints}
					errorCode={this.state.errorCode}

					onSubmit={this.handleFormSubmitted}
					onCancelClick={this.handleCancelClick}
					isProcessing={this.state.isProcessing}
				/>
			</div>
		)
	}
}

ReactDOM.render(
	<App />,
	document.getElementById('root')
);

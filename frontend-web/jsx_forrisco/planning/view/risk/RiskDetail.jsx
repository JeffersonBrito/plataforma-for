import _ from 'underscore';
import React from "react";
import { Link } from 'react-router';
import Toastr from 'toastr';

import Messages from "forpdi/jsx/core/util/Messages.jsx";
import RiskRegister from 'forpdi/jsx_forrisco/planning/view/risk/RiskRegister.jsx';
import PreventiveActions from 'forpdi/jsx_forrisco/planning/view/risk/PreventiveActions';
import Monitor from 'forpdi/jsx_forrisco/planning/view/risk/Monitor';
import Incident from 'forpdi/jsx_forrisco/planning/view/risk/Incident';
import Contingency from 'forpdi/jsx_forrisco/planning/view/risk/Contingency';
import LoadingGauge from "forpdi/jsx/core/widget/LoadingGauge.jsx";
import RiskStore from 'forpdi/jsx_forrisco/planning/store/Risk.jsx';
import Modal from "forpdi/jsx/core/widget/Modal.jsx";
import PermissionsTypes from "forpdi/jsx/planning/enum/PermissionsTypes.json";

const tabs = {
	RISK_REGISTER: 0,
	MONITOR: 1,
	INCIDENT: 2,
	CONTINGENCY: 3,
};

export default React.createClass({

	contextTypes: {
		router: React.PropTypes.object.isRequired,
		roles: React.PropTypes.object.isRequired,
		tabPanel: React.PropTypes.object,
		permissions: React.PropTypes.array.isRequired,
	},
	childContextTypes: {
		policy: React.PropTypes.object,
		tabPanel: React.PropTypes.object,
		planRisk: React.PropTypes.object
	},
	propTypes: {
		policy: React.PropTypes.object.isRequired
	},

	getInitialState() {
		return {
			tabs: [],
			tabsHash: '',
			tabsHidden: [],
			showTabsHidden: false,
			selected: this.props.selected ? this.props.selected : tabs.RISK_REGISTER,
			riskModel: null,
			visualization: true,
			loading: true,
		};
	},

	componentDidMount() {
		var me = this;
		RiskStore.on("findRisk", (model) => {
			if (model.success && (this.state.riskModel == null || model.data.id != this.state.riskModel.id)) {
				this.setState({
					riskModel: model.data,
					loading: false
				})
			}

			_.defer(() => {
				this.context.tabPanel.addTab(this.props.location.pathname, this.state.riskModel.name);
			});
		}, me);

		RiskStore.on("riskDelete", (model) => {
			if (model.success) {
				const hasMinTabsLength = this.context.tabPanel.state.tabs.length <= 1 ? true : false;
				this.context.tabPanel.removeTabByPath(this.props.location.pathname);

				if (hasMinTabsLength) {
					this.context.router.push("forrisco/plan-risk/" + this.props.params.planRiskId + "/unit/" + this.props.params.unitId + "/info");
				}

			} else {
				var errorMsg = JSON.parse(model.responseText);
				Toastr.error(errorMsg.message);
			}
		}, me);
		this.refresh(this.props)
	},

	componentWillReceiveProps(newProps) {
		if (this.props.params.riskId !== newProps.params.riskId) {
			this.refresh(newProps)
		}
	},

	componentWillUnmount() {
		RiskStore.off(null, null, this);
	},

	refresh(newProps) {
		RiskStore.dispatch({
			action: RiskStore.ACTION_FIND_RISK,
			data: newProps.params.riskId
		})
	},

	renderUnarchiveRisk() {

		return (
			<ul id="level-menu" className="dropdown-menu">
				<li>
					<Link
						onClick={this.onChange}>
						<span className="mdi mdi-pencil cursorPointer"
							title={Messages.get("label.title.editInformation")}>
							<span
								id="menu-levels"> {Messages.getEditable("label.title.editInformation", "fpdi-nav-label")} </span>
						</span>
					</Link>
				</li>
				{this.state.undeletable ?
					<li>
						<Link>
							<span className="mdi mdi-delete disabledIcon cursorPointer"
								title={Messages.get("label.notDeletedHasChild")}>
								<span id="menu-levels"> {Messages.getEditable("label.deleteRisk", "fpdi-nav-label")}</span>
							</span>
						</Link>
					</li>
					:
					<li>
						<Link
							onClick={this.deleteRisco}>
							<span className="mdi mdi-delete cursorPointer" title={Messages.get("label.deleteRisk")}>
								<span id="menu-levels"> {Messages.getEditable("label.deleteRisk", "fpdi-nav-label")} </span>
							</span>
						</Link>
					</li>
				}
			</ul>
		);
	},

	deleteRisco() {
		var me = this;
		if (me.state.riskModel != null) {
			var msg = "Você tem certeza que deseja excluir esse Risco?"
			Modal.confirmCustom(() => {
				Modal.hide();
				RiskStore.dispatch({
					action: RiskStore.ACTION_DELETE,
					data: me.state.riskModel.id
				});
			}, msg, () => {
				Modal.hide()
			});
		}
	},

	onChange() {
		if (this.state.selected === tabs.RISK_REGISTER) {
			this.setState({
				visualization: !this.state.visualization,
			});
		} else {
			this.setState({
				selected: tabs.RISK_REGISTER,
				visualization: false,
			});
		}
	},

	selectInfo() {

		switch (this.state.selected) {
			case tabs.RISK_REGISTER:
				return (
					<div>
						<RiskRegister
							{...this.props}
							visualization={this.state.visualization}
							risk={this.state.riskModel}
							onChange={this.onChange}
						/>
						<PreventiveActions
							visualization={this.state.visualization}
							risk={this.state.riskModel}
							planRiskId={this.props.params.planRiskId}
						/>
					</div>
				)

			case tabs.MONITOR:
				return (
					<Monitor
						visualization={this.state.visualization}
						risk={this.state.riskModel}
						planRiskId={this.props.params.planRiskId}
					/>)

			case tabs.INCIDENT:
				return (
					<Incident
						visualization={this.state.visualization}
						risk={this.state.riskModel}
						planRiskId={this.props.params.planRiskId}
					/>)

			case tabs.CONTINGENCY:
				return (
					<Contingency
						visualization={this.state.visualization}
						risk={this.state.riskModel}
					/>)
		}
	},

	setInfo(select) {
		this.setState({
			selected: select
		})
	},

	header() {

		return (<div style={{ "display": "flex" }}>
			<div className={"frisco-link icon-link " + (this.state.selected == 0 ? "selecionado" : "")}
				onClick={() => this.setInfo(0)}>
				Informações
				</div>

			<div className={"frisco-link icon-link " + (this.state.selected == 1 ? "selecionado" : "")}
				onClick={() => this.setInfo(1)}>
				Monitoramento
				</div>

			<div className={"frisco-link icon-link " + (this.state.selected == 2 ? "selecionado" : "")}
				onClick={() => this.setInfo(2)}>
				Incidente
				</div>

			<div className={"frisco-link icon-link " + (this.state.selected == 3 ? "selecionado" : "")}
				onClick={() => this.setInfo(3)}>
				Contingenciamento
				</div>
		</div>
		)
	},

	render() {
		if (this.state.loading) {
			return <LoadingGauge />;
		}

		return (<div className="fpdi-card fpdi-card-full floatLeft">
			<h1>
				{this.state.riskModel ? this.state.riskModel.name : "Risco não encontrado"}
				{
					(this.context.roles.MANAGER ||
						_.contains(this.context.permissions, PermissionsTypes.FORRISCO_MANAGE_RISK_PERMISSION))
					&&
					<span className="dropdown">
						<a className="dropdown-toggle"
							data-toggle="dropdown"
							aria-haspopup="true"
							aria-expanded="true"
							title={Messages.get("label.actions")}
						>
							<span className="sr-only">{Messages.getEditable("label.actions", "fpdi-nav-label")}</span>
							<span className="mdi mdi-chevron-down" />
						</a>
						{this.renderUnarchiveRisk()}
					</span>
				}
			</h1>
			<div>
				{this.header()}
			</div>
			{this.selectInfo()}
		</div>);
	}
});

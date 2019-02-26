import React from "react";
import PlanRiskItemStore from "forpdi/jsx_forrisco/planning/store/PlanRiskItem.jsx"
import EditPlanRiskSubItem from "forpdi/jsx_forrisco/planning/view/plan/item/subitem/EditPlanRiskSubItem.jsx";
import {Link} from "react-router";
import Form from "@/planning/widget/attributeForm/AttributeForm";
import Validation from "forpdi/jsx_forrisco/core/util/Validation";
import LoadingGauge from "forpdi/jsx/core/widget/LoadingGauge.jsx";
import _ from "underscore";
import Messages from "@/core/util/Messages";
import EditPlanRiskItem from "forpdi/jsx_forrisco/planning/view/plan/item/DetailPlanRiskItem";

var VerticalForm = Form.VerticalForm;
var Validate = Validation.validate;

export default React.createClass({
	contextTypes: {
		accessLevel: React.PropTypes.number.isRequired,
		roles: React.PropTypes.object.isRequired,
		router: React.PropTypes.object,
		toastr: React.PropTypes.object.isRequired,
		tabPanel: React.PropTypes.object,
		permissions: React.PropTypes.array.isRequired,
		planRisk: React.PropTypes.object.isRequired
	},

	propTypes: {
		location: React.PropTypes.object.isRequired
	},

	getInitialState() {
		return {
			itemTitle: "",
			field: [{
				fieldName: [],
				fieldContent: [],
				isText: ""
			}],
			tabPath: "",
			isLoading: true,
			onEdit: false
		};
	},

	componentDidMount() {
		var content = [];
		PlanRiskItemStore.on('detailSubItem', response => {
			if (response.data.length !== 0) {

				response.data.planRiskSubItemField.map(field => {
					content.push({
						fieldName: field.name,
						fieldContent: field.description,
						isText: field.isText,
						fileLink: field.fileLink,
						editInstance: false,
						required: true
					})
				});

				this.setState({
					itemTitle: response.data.name,
					field: content,
					isLoading: false,
					tabPath: this.props.location.pathname,
					onEdit: false
				});

			} else {

				this.setState({
					itemTitle: response.data.name,
					field: [],
					isLoading: false,
					tabPath: this.props.location.pathname,
					onEdit: false
				});
			}
		}, this);
		this.refreshComponent(this.props.params.subItemId);
	},

	componentWillReceiveProps(newProps) {
		if (this.props.params.subItemId !== newProps.subItemId) {
			this.refreshComponent(newProps.subItemId);
		}

		if (this.state.tabPath !== this.props.location.pathname) {
			// _.defer(() => {
			// 	this.context.tabPanel.addTab(
			// 		this.props.location.pathname,
			// 		this.state.itemTitle.length > 15 ? this.state.itemTitle.substring(0, 15) + "..." :
			// 			this.state.itemTitle.substring(0, 15)
			// 	);
			// });
		}

	},

	refreshComponent(subItemId) {
		PlanRiskItemStore.dispatch({
			action: PlanRiskItemStore.ACTION_DETAIL_SUBITEM,
			data: {
				id: subItemId
			},
		});
	},

	onEdit() {
		this.setState({
			onEdit: true
		})
	},

	renderDropdown() {
		return (
			<ul id="level-menu" className="dropdown-menu">
				<li>
					<Link>
						<span className="mdi mdi-pencil cursorPointer" title={Messages.get("label.title.editPolicy")}>
							<span id="menu-levels" onClick={this.onEdit}> Editar Informações </span>
						</span>
					</Link>
				</li>
				<li>
					<Link onClick={this.deletePlanRisk}>
					<span className="mdi mdi-delete cursorPointer" title={Messages.get("label.deletePolicy")}>
						<span id="menu-levels"> Deletar Item </span>
					</span>
					</Link>
				</li>
			</ul>
		)
	},

	renderBreadcrumb() {
		return (
			<div>
				<span>
					<Link className="fpdi-breadcrumb fpdi-breadcrumbDivisor"
						  to={'/forrisco/plan-risk/' + this.props.params.planRiskId + '/item/' + this.props.params.planRiskId + '/info'}
						  title={this.context.planRisk.attributes.name}>
						{
							this.context.planRisk.attributes.name.length > 15 ?
								this.context.planRisk.attributes.name.substring(0, 15) + "..." :
								this.context.planRisk.attributes.name.substring(0, 15)
						}
					</Link>
					<span className="mdi mdi-chevron-right fpdi-breadcrumbDivisor"/>
				</span>
				<span className="fpdi-breadcrumb fpdi-selectedOnBreadcrumb">
					{
						this.state.itemTitle.length > 15 ?
							this.state.itemTitle.substring(0, 15) + "..." :
							this.state.itemTitle.substring(0, 15)
					}
				</span>
			</div>
		)
	},

	render() {

		if (this.state.isLoading === true) {
			return <LoadingGauge/>;
		}

		return (
			<div>
				{this.renderBreadcrumb()}

				<div className="fpdi-card fpdi-card-full floatLeft">
					<h1>
						{this.state.itemTitle}
						<span className="dropdown">
							<a className="dropdown-toggle" data-toggle="dropdown" aria-haspopup="true"
							   aria-expanded="true"
							   title={Messages.get("label.actions")}>

								<span
									className="sr-only">{Messages.getEditable("label.actions", "fpdi-nav-label")}</span>
								<span className="mdi mdi-chevron-down"/>

							</a>

							{this.renderDropdown()}
						</span>
					</h1>
					{
						this.state.onEdit === false ?

							this.state.field.map((field, key) => {

								if (field.isText === true) {
									return (
										<div className="form-group form-group-sm" key={key}>
											<label className="fpdi-text-label"> {field.fieldName} </label>

											<div>
												<span className="pdi-normal-text"> {field.fieldContent} </span>
											</div>
										</div>
									)
								} else {
									return (
										<div className="form-group form-group-sm" key={key}>
											<label className="fpdi-text-label"> {field.fieldName} </label>

											<div className="panel panel-default">
												<table className="budget-field-table table">
													<tbody>
													<tr>
														<td className="fdpi-table-cell">
															<a target="_blank" rel="noopener noreferrer"
															   href={field.fileLink}>
																{field.fieldContent}
															</a>
														</td>
													</tr>
													</tbody>
												</table>
											</div>
										</div>
									)
								}
							})
							: <EditPlanRiskSubItem
								itemTitle={this.state.itemTitle}
								fieldsValues={this.state.field}
								onEdit={this.state.onEdit}
								itemId={this.props.params.itemId}
								subitemId={this.props.params.subItemId}
								planRiskId={this.props.params.planRiskId}
							/>

					}
				</div>
			</div>
		)
	}
});
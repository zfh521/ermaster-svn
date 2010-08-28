package org.insightech.er.extention;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.insightech.er.Activator;
import org.insightech.er.editor.ERDiagramEditor;

/**
 * ExtensionLoader��ERDaiagramEditor�̃A�_�v�^
 */
public class ExtensionLoaderAdapter {

	private ExtensionLoader loader;

	public ExtensionLoaderAdapter() {
		try {
			this.loader = new ExtensionLoader();
			loader.loadExtensions();

		} catch (CoreException e) {
			Activator.showExceptionDialog(e);
		}
	}

	/**
	 * ExtensionLoader����ǂݍ��񂾃N���X��ERDiagramEditor��ActionRegister�ɒǉ�����
	 * 
	 * @param editor
	 *            �ǉ������ERDiagramEditor
	 * @param registry
	 *            ActionRegister
	 * @param selectionActionList
	 *            �ǉ������Action��ID���X�g
	 */
	public void addActions(ERDiagramEditor editor, ActionRegistry registry,
			List<String> selectionActionList) {
		Map objMap = loader.getObjMap();
		List nameList = loader.getNameList();

		for (int i = 0; i < nameList.size(); i++) {
			IExtendAction exaction = (IExtendAction) objMap
					.get(nameList.get(i));

			IAction action = exaction.createIAction(editor, (String) nameList
					.get(i));
			selectionActionList.add(action.getId());
			registry.registerAction(action);
		}
	}

	/**
	 * MenuManager��ExtentionLoader����ǂݍ��񂾃N���X��ǉ�
	 * 
	 * @param menuMgr
	 *            �ǉ������|�b�v�A�b�v���j���[�̃}�l�W���[
	 * @param actionregistry
	 *            �|�b�v�A�b�v���j���[�ɒǉ�����A�N�V������ǂݍ���
	 */
	public void addERDiagramPopupMenu(MenuManager menuMgr,
			ActionRegistry actionregistry) {
		List nameList = loader.getNameList();
		Map pathMap = loader.getPathMap();
		Map objMap = loader.getObjMap();
		for (int i = 0; i < nameList.size(); i++) {
			try {
				IAction action = actionregistry
						.getAction(((IExtendAction) objMap.get(nameList.get(i)))
								.getId());
				menuMgr
						.findMenuUsingPath(
								(String) pathMap.get(nameList.get(i))).add(
								action);
			} catch (NullPointerException e) {
				IAction action = actionregistry
						.getAction(((IExtendAction) objMap.get(nameList.get(i)))
								.getId());
				menuMgr.add(action);
			}
		}
	}

	public ExtensionLoader getLoader() {
		return loader;
	}

}
package com.yeemos.app.interfaces;

public interface IUpdateUI {
    /**
     * UI_SHOW_EMPTRY:显示无数据的页面
     * UI_SHOW_NO_MORE_DATA：显示没有更多数据的页面
     * UI_SHOW_MORE_DATA：显示还有数据
     * @author apple
     *
     */
    public enum UI_SHOW_TYPE {
        UI_SHOW_EMPTY(0),
        UI_SHOW_NO_MORE_DATA(1),
        UI_SHOW_MORE_DATA(2);

        int nValues;
        private UI_SHOW_TYPE(int i){nValues = i;}
        public int GetValues(){return nValues;}

        public boolean Compare(int nNum){return nValues == nNum;}
        public static UI_SHOW_TYPE GetObject(int nNum)
        {
            UI_SHOW_TYPE[] As = UI_SHOW_TYPE.values();
            for(int i = 0; i < As.length; i++)
            {
                if(As[i].Compare(nNum))
                    return As[i];
            }
            return UI_SHOW_EMPTY;
        }
    }



    /**
     * 刷新UI
     * @param isShowEmptyUI: true:表示无数据,需要显示无数据的页面 false:显示有数据的页面
     * @return
     */
    public boolean refreshUIview(UI_SHOW_TYPE showType);

    /**
     * 接受到更改语言设置后执行的方法
     */
    public void updateUIText();



    /**
     * 更新数据，
     * @param bIsClearData:是否需要把之前的数据清楚
     * @return:
     */
    public UI_SHOW_TYPE updateData(boolean bIsClearData);


}

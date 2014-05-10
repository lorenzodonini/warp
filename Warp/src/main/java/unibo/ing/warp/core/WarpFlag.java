package unibo.ing.warp.core;

/**
 * Created by Lorenzo Donini on 5/1/2014.
 */
public enum WarpFlag {
    MASTER {
        private boolean bMaster=false;
        private boolean bFinal=false;

        @Override
        public void setValue(Object value)
        {
            if(!bFinal && value instanceof Boolean)
            {
                bFinal=true;
                bMaster=(Boolean)value;
            }
        }

        @Override
        public Object getValue()
        {
            return bMaster;
        }
    }, BUFFER_SIZE {
        private int mBufferSize = 0;

        @Override
        public void setValue(Object value)
        {
            if(value instanceof Integer)
            {
                mBufferSize = (Integer)value;
            }
        }

        @Override
        public Object getValue()
        {
            return mBufferSize;
        }
    }, TIMEOUT {
        private int mTimeout=DEFAULT;
        private static final int DEFAULT=5;

        @Override
        public void setValue(Object value)
        {
            if(value instanceof Integer)
            {
                mTimeout=(Integer)value;
            }
        }

        @Override
        public Object getValue()
        {
            return mTimeout;
        }
    }, LINGER {
        private int mLingerTime=DEFAULT;
        private static final int DEFAULT=5;

        @Override
        public void setValue(Object value)
        {
            if(value instanceof Integer)
            {
                mLingerTime=(Integer)value;
            }
        }

        @Override
        public Object getValue()
        {
            return mLingerTime;
        }
    };

    public abstract void setValue(Object value);
    public abstract Object getValue();
}

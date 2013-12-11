/*
 *  Copyright 2011 Yuri Kanivets
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package kankan.wheel.widget.adapters;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;

import java.util.LinkedList;
import java.util.List;

/**
 * Abstract Wheel adapter.
 */
public abstract class AbstractWheelAdapter implements WheelViewAdapter {
    // Observers
    private List<DataSetObserver> datasetObservers;

    @Override
    public View getEmptyItem(final View convertView, final ViewGroup parent) {
        return null;
    }

    /**
     * Notifies observers about data changing
     */
    protected void notifyDataChangedEvent() {
        if (this.datasetObservers != null) {
            for (final DataSetObserver observer : this.datasetObservers) {
                observer.onChanged();
            }
        }
    }

    /**
     * Notifies observers about invalidating data
     */
    protected void notifyDataInvalidatedEvent() {
        if (this.datasetObservers != null) {
            for (final DataSetObserver observer : this.datasetObservers) {
                observer.onInvalidated();
            }
        }
    }

    @Override
    public void registerDataSetObserver(final DataSetObserver observer) {
        if (this.datasetObservers == null) {
            this.datasetObservers = new LinkedList<DataSetObserver>();
        }
        this.datasetObservers.add(observer);
    }

    @Override
    public void unregisterDataSetObserver(final DataSetObserver observer) {
        if (this.datasetObservers != null) {
            this.datasetObservers.remove(observer);
        }
    }
}

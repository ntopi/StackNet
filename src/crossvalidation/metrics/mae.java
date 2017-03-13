/*Copyright (c) 2017 Marios Michailidis

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/


package crossvalidation.metrics;
import exceptions.IllegalStateException;


/**
 * 
 * @author marios
 *<p> Class used to calculate the Mean Absolute Error defined as:
 * <pre> abs(S<sub>i=1</sub>(X<sub>i</sub>-Y<sub>i</sub>)/N)</pre></p>
 */
public class mae implements Metric {
    /**
     * The current_metrics' value
     */
	private double metric=0.0;

	public double GetValue(double[] predicted, double[] actual) {
		//sensible checks
		if (actual==null || predicted==null || actual.length==0 || predicted.length==0 || actual.length!=predicted.length){
			throw new IllegalStateException(" There is an error with the state of actual and pred in terms of length ");
		}
 		double errormae=0;
		for (int i=0; i <predicted.length; i++ ) {
			errormae+=Math.abs(actual[i]-predicted[i]);
		}
		errormae=errormae/actual.length;          	 
		metric=errormae;
		return errormae;
	}

	@Override
	public double[] GetValue(double[][] predicted, double[][] actual,int threads) {
		
		//sensible checks
		if (actual==null || predicted==null || actual.length==0 || predicted.length==0 || actual.length!=predicted.length || predicted[0].length!=actual[0].length){
			throw new IllegalStateException(" There is an error with the state of actual and pred in terms of dimensions ");
		}		
		// thread checks
		
		if (threads<=0) {
			threads=1;
		}
		
		if (threads>predicted[0].length) {
			threads=predicted[0].length;
		}	
		// initialize the new arry to copy to
		double new_array_to_copy_to []= new double [predicted[0].length];
	
		// the threads of operations
		
		Thread[] thread_array= new Thread[threads];
		int count_of_live_threads=0;
		
		for (int n=0; n <new_array_to_copy_to.length; n++ ){
			thread_array[count_of_live_threads]= new Thread(new Metrics2dRunnable(actual,predicted, new_array_to_copy_to, n,"mae"));
			thread_array[count_of_live_threads].start();
			count_of_live_threads++;
			if (count_of_live_threads==threads || n==new_array_to_copy_to.length-1){
				for (int s=0; s <count_of_live_threads;s++ ){
					try {
						thread_array[s].join();
					} catch (InterruptedException e) {
					   System.out.println(e.getMessage());
					}
				}
				count_of_live_threads=0;
			}
		}
		
	    metric=Stats.DescriptiveStatistics.getMean(new_array_to_copy_to);
		return  new_array_to_copy_to ;
	}

	@Override
	public double GetValue() {
		return metric;
	}

	@Override
	public boolean IsBetter(double value) {
		if (value>metric){
			return true;
		}else {
		return false;
		}
	}

	@Override
	public void UpdateValue(double value) {
		metric=value;
		
	}
	
	@Override
	public boolean IsBetter(Metric m) {
		if ( !m.Gettype().equals(this.Gettype())){
			throw new IllegalStateException(" Metrics are not comparable as: " + m.Gettype() + " <> " + this.Gettype());
		}
		if (m.GetValue()<metric){
			return true;
		}else {
		return false;
		}
	}

	@Override
	public String Gettype() {
		return "mae";
	}


}
